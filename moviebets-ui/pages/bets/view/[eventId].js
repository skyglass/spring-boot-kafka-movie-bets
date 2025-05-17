import Link from 'next/link';
import withAuth from '../../../auth/middleware/withAuth';
import buildClient from "../../../api/build-client";
import { useKeycloak } from '../../../auth/provider/KeycloakProvider';
import { useEffect, useState } from "react";
import { useRouter } from "next/router";

const BetListPage = () => {
    const router = useRouter();
    const [bets, setBets] = useState([]);
    const { user } = useKeycloak();
    const { eventId } = router.query;
    const [event, setEvent] = useState(null);
    const [errors, setErrors] = useState(null);

    useEffect(() => {
        if (user) {
            const fetchData = async () => {

                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/bet/get-bets-for-market/${eventId}`);
                    setBets(data.betDataList);
                } catch (err) {
                    setErrors(err.response?.data?.errors || [{ message: "Failed to fetch event" }]);
                }
            };
            fetchData();
        }
    }, [user, eventId]);

    useEffect(() => {
        if (user) {
            const fetchData = async () => {

                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/market/get-state/${eventId}`);
                    setEvent(data);
                } catch (err) {
                    setErrors(err.response?.data?.errors || [{ message: "Failed to fetch event" }]);
                }
            };
            fetchData();
        }
    }, [user, eventId]);

    const getResultText = (result) => {
        switch (result) {
            case "ITEM1_WINS":
                return "Movie 1 Wins";
            case "ITEM2_WINS":
                return "Movie 2 Wins";
            default:
                return "Unknown";
        }
    };

    const betList = bets.map((bet) => {
        return (
            <tr key={bet.betId}>
                {/* Event Name */}
                <td>{bet.marketName}</td>

                {/* User */}
                <td>{bet.customerId}</td>

                {/* Result */}
                <td>{getResultText(bet.result)}</td>

                {/* Link to view bet details */}
                <td>
                    <Link href={`/bets/${bet.betId}`}>
                        View Bet
                    </Link>
                </td>
            </tr>
        );
    });

    if (!event) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>List of Bets for Event "{event.item1} vs {event.item2}"</h1>
            <Link href="/bets/place">
                <button className="btn btn-primary" style={{ marginBottom: '10px' }}>Place a Bet</button>
            </Link>
            <table className="table">
                <thead>
                <tr>
                    <th>Event Name</th>
                    <th>User</th>
                    <th>Result</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>{betList}</tbody>
            </table>
        </div>
    );
};

export default withAuth(BetListPage);