import Link from 'next/link';
import withAuth from '../auth/middleware/withAuth';
import buildClient from "../api/build-client";
import { useKeycloak } from '../auth/provider/KeycloakProvider';
import { useEffect, useState } from "react";
import { getEventResultText } from "../helpers/Global";

const LandingPage = () => {
    const [events, setEvents] = useState([]);
    const { user } = useKeycloak();

    useEffect(() => {
        if (user) {
            const fetchData = async () => {
                const client = buildClient({ req: {}, currentUser: user });
                const { data } = await client.get('/api/market/all');
                setEvents(data);
            };
            fetchData();
        }
    }, [user]);

    const eventList = events.map((event) => {
        return (
            <tr key={event.marketId}>
                <td>{event.item1}</td>
                <td>{event.item2}</td>

                <td>{getEventResultText(event.open, event.result)}</td>

                <td>{new Date(event.closesAt).toLocaleString()}</td>

                <td>
                    <Link href={`/events/${event.marketId}`}>
                        View
                    </Link>
                </td>

                <td>
                    <Link href={`/bets/place/${event.marketId}`}>
                        Place Bet
                    </Link>
                </td>

                <td>
                    <Link href={`/bets/view/event/${event.marketId}`}>
                        View Bets
                    </Link>
                </td>
            </tr>
        );
    });

    return (
        <div>
            <h4>Movie Events</h4>
            <Link href="/events/new">
                <button className="btn btn-primary" style={{ marginBottom: '10px' }}>Add Movie Event</button>
            </Link>
            <table className="table">
                <thead>
                <tr>
                    <th>Movie 1</th>
                    <th>Movie 2</th>
                    <th>Result</th>
                    <th>Close Time</th>
                    <th>Link</th>
                    <th>Action</th>
                    <th>View Bets</th>
                </tr>
                </thead>
                <tbody>{eventList}</tbody>
            </table>
        </div>
    );
};

export default withAuth(LandingPage);