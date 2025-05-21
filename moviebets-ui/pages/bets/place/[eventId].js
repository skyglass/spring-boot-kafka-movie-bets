import { useRouter } from "next/router";
import { useState, useEffect } from "react";
import {useKeycloak} from "../../../auth/provider/KeycloakProvider";
import buildClient from "../../../api/build-client";
import { v4 as uuidv4 } from 'uuid';

const NewBet = () => {
  const router = useRouter();
  const { eventId } = router.query;
  const { user } = useKeycloak();

  const [event, setEvent] = useState(null);
  const [selectedResult, setSelectedResult] = useState(0);

  useEffect(() => {
    if (!eventId) return;

    const fetchEvent = async () => {
      try {
        const client = buildClient({ req: {}, currentUser: user });
        const response = await client.get(`/api/market/get-state/${eventId}`);
        setEvent(response.data);
      } catch (error) {
        console.error("Error fetching event:", error);
      }
    };

    fetchEvent();
  }, [eventId]);

  if (!event) return <p>Loading event details...</p>;

  // Map result to corresponding odds
  const resultOptions = [
    { value: 0, label: event.item1 },
    { value: 1, label: event.item2 },
  ];

  const handleStakeChange = (e) => {
    const value = e.target.value;
    if (/^\d*$/.test(value) && Number(value) > 0) {
      setStake(value);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const requestId = uuidv4();
    const cancelRequestId = uuidv4();

    const betData = {
      betId: uuidv4(),
      marketId: eventId,
      marketName: `${event.item1} vs ${event.item2}`,
      customerId: user.name,
      result: selectedResult,
      requestId: requestId,
      cancelRequestId: cancelRequestId,
    };

    try {
      const client = buildClient({ req: {}, currentUser: user });
      const response = await client.post('/api/bet/open', betData);

      if (response.status !== 201) {
        const errorMessage = await response.data.message; // Get error message from server
        console.log("Failed to place bet:", errorMessage);
        return;
      }

      router.push(`/bets/view/event/${eventId}`);
    } catch (error) {
      console.error("Bet submission failed:", error);
    }
  };

  return (
      <div className="container mt-5 p-4 bg-white shadow rounded">
        <h2 className="h2 font-weight-bold mb-4">Place a Bet</h2>

        <form onSubmit={handleSubmit}>

          {/* Result Selection */}
          <div className="mb-4">
            <label className="form-label font-weight-semibold">Select Movie</label>
            <select
                className="form-select"
                value={selectedResult}
                onChange={(e) => setSelectedResult(Number(e.target.value))}
            >
              {resultOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
              ))}
            </select>
          </div>

          {/* Submit Button */}
          <button
              type="submit"
              className="btn btn-primary w-100"
          >
            Place Bet
          </button>
        </form>
      </div>
  );
};

export default NewBet;