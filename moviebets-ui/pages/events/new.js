import { useState, useEffect } from 'react';
import Router from 'next/router';
import { useKeycloak } from "../../auth/provider/KeycloakProvider";
import buildClient from "../../api/build-client";
import { v4 as uuidv4 } from 'uuid';
import { useMessage } from "../../provider/MessageContextProvider";

const NewEvent = () => {
  const { user } = useKeycloak();
  const [item1, setItem1] = useState('');
  const [item2, setItem2] = useState('');
  const [durationValue, setDurationValue] = useState('');
  const [durationUnit, setDurationUnit] = useState('minutes');
  const [errors, setErrors] = useState(null);
  const [closesAtPreview, setClosesAtPreview] = useState('');
  const { showMessage } = useMessage();

  const computeClosesAt = () => {
    const value = parseInt(durationValue, 10);
    if (isNaN(value) || value <= 0) {
      return null;
    }

    const now = Date.now();
    let millis = 0;

    switch (durationUnit) {
      case 'seconds':
        millis = value * 1000;
        break;
      case 'minutes':
        millis = value * 60 * 1000;
        break;
      case 'hours':
        millis = value * 60 * 60 * 1000;
        break;
      case 'days':
        millis = value * 24 * 60 * 60 * 1000;
        break;
      default:
        return null;
    }

    return new Date(now + millis).toISOString();
  };

  useEffect(() => {
    const result = computeClosesAt();
    if (result) {
      setClosesAtPreview(new Date(result).toLocaleString());
    } else {
      setClosesAtPreview('');
    }
  }, [durationValue, durationUnit]);

  const onSubmit = async (event) => {
    event.preventDefault();
    setErrors(null);

    const validationErrors = [];

    if (!item1.trim()) {
      validationErrors.push({ message: "Movie 1 must not be empty" });
    }

    if (!item2.trim()) {
      validationErrors.push({ message: "Movie 2 must not be empty" });
    }

    const closesAt = computeClosesAt();
    if (!closesAt) {
      validationErrors.push({ message: "Please enter a valid duration greater than 0" });
    }

    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }

    try {
      const client = buildClient({ req: {}, currentUser: user });

      const data = {
        marketId: uuidv4(),
        item1,
        item2,
        closesAt,
      };

      await client.post('/api/market/open', data);
      Router.push('/');
    } catch (error) {
      const errorMsg =
          error.response?.data?.message ||
          error.message ||
          "Unexpected error opening the event.";
      showMessage(errorMsg, 'error');
    }
  };

  return (
      <div>
        <h1>Create Event</h1>
        <form onSubmit={onSubmit}>
          <div className="form-group">
            <h3>Movie Bet Details</h3>
            <label>Movie 1</label>
            <input
                value={item1}
                onChange={(e) => setItem1(e.target.value)}
                className="form-control"
            />
          </div>
          <div className="form-group">
            <label>Movie 2</label>
            <input
                value={item2}
                onChange={(e) => setItem2(e.target.value)}
                className="form-control"
            />
          </div>

          <div className="form-group">
            <label>Closes In:</label>
            <div className="d-flex">
              <input
                  type="number"
                  min="1"
                  step="1"
                  value={durationValue}
                  onChange={(e) => setDurationValue(e.target.value)}
                  className="form-control me-2"
                  placeholder="Duration"
              />
              <select
                  value={durationUnit}
                  onChange={(e) => setDurationUnit(e.target.value)}
                  className="form-control"
              >
                <option value="seconds">Seconds</option>
                <option value="minutes">Minutes</option>
                <option value="hours">Hours</option>
                <option value="days">Days</option>
              </select>
            </div>
            {closesAtPreview && (
                <small className="form-text text-muted mt-1">
                  Will close at: <strong>{closesAtPreview}</strong>
                </small>
            )}
          </div>

          {errors && (
              <div className="alert alert-danger mt-3">
                <ul>
                  {errors.map((err, index) => (
                      <li key={index}>{err.message}</li>
                  ))}
                </ul>
              </div>
          )}

          <button className="btn btn-primary mt-3">Submit</button>
        </form>
      </div>
  );
};

export default NewEvent;