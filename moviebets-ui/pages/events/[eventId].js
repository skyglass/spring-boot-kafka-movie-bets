import { useEffect, useState } from 'react';
import Router from 'next/router';
import { useKeycloak } from '../../auth/provider/KeycloakProvider';
import buildClient from '../../api/build-client';
import { useRouter } from 'next/router';

const EventShow = () => {
    const { user } = useKeycloak();
    const router = useRouter();
    const { eventId } = router.query;
    const [event, setEvent] = useState(null);
    const [errors, setErrors] = useState(null);

    useEffect(() => {
        if (user && eventId) {
            const fetchEvent = async () => {
                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/market/get-state/${eventId}`);
                    setEvent(data);
                } catch (err) {
                    setErrors(err.response?.data?.errors || [{ message: "Failed to fetch event" }]);
                }
            };
            fetchEvent();
        }
    }, [user, eventId]);

    const placeBet = async () => {
        setErrors(null);
        try {
            const client = buildClient({ req: {}, currentUser: user });
            const { data: bet } = await client.post('/api/bet', { marketId: event.id });
            Router.push('/bets/[betId]', `/bets/${bet.id}`);
        } catch (err) {
            setErrors(err.response?.data?.errors || [{ message: "Failed to place bet" }]);
        }
    };

    const getResultText = (open, result) => {
        if (open) {
            return "OPEN";
        }
        return result;
    };

    if (!event) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>{event.item1} vs {event.item2}</h1>

            {errors && (
                <div className="alert alert-danger">
                    <ul>
                        {errors.map((err, index) => (
                            <li key={index}>{err.message}</li>
                        ))}
                    </ul>
                </div>
            )}

            <h3>Movie Bet Information</h3>
            <div className="market-data">
                <div>
                    <p><strong>Movie 1:</strong> {event.item1}</p>
                    <p><strong>Movie 2:</strong> {event.item2}</p>
                </div>
                <div className="additional-info">
                    {event.closesAt && (
                        <p><strong>Closes At:</strong> {new Date(event.closesAt).toLocaleString()}</p>
                    )}
                </div>
            </div>

            <button onClick={placeBet} className="btn btn-primary">
                Place Bet
            </button>
        </div>
    )
};

export default EventShow;