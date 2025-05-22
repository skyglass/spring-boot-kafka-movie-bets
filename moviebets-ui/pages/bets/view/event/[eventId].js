import Link from 'next/link';
import withAuth from '../../../../auth/middleware/withAuth';
import buildClient from "../../../../api/build-client";
import { useKeycloak } from '../../../../auth/provider/KeycloakProvider';
import { useEffect, useState } from "react";
import { useRouter } from "next/router";
import { getResultText, getBetWon } from "../../../../helpers/Global";
import { useMessage } from "../../../../provider/MessageContextProvider";
import { isAdminFunc } from "../../../../auth/components/Helpers";

const BetListPage = () => {
    const router = useRouter();
    const [bets, setBets] = useState([]);
    const [votesCount, setVotesCount] = useState(null);
    const { user } = useKeycloak();
    const { eventId } = router.query;
    const [event, setEvent] = useState(null);
    const { showMessage } = useMessage();

    useEffect(() => {
        if (user) {
            const fetchData = async () => {

                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const endpoint = isAdminFunc(user)
                        ? `/api/bet/get-bets-for-market-for-admin/${eventId}`
                        : `/api/bet/get-bets-for-market/${eventId}`;
                    const { data } = await client.get(endpoint);
                    setBets(data.betDataList);
                } catch (error) {
                    if (error.response?.status === 409) {
                        // Market is open, show vote count instead of error
                        try {
                            const client = buildClient({ req: {}, currentUser: user });
                            const marketStatus = await client.get(`/api/bet/get-market-status/${eventId}`);
                            setVotesCount(marketStatus.data.votes);
                        } catch (statusError) {
                            console.error("Failed to fetch market status", statusError);
                        }
                    } else {
                        const errorMsg = error.response?.data?.message || error.message || "Unexpected error when getting bets for market.";
                        showMessage(errorMsg, 'error');
                    }
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
                } catch (error) {
                    const errorMsg = error.response?.data?.message || error.message || "Unexpected error when fetching event.";
                    showMessage(errorMsg, 'error');
                }
            };
            fetchData();
        }
    }, [user, eventId]);

    const betList = bets.map((bet) => {
        return (
            <tr key={bet.betId}>
                {/* Event Name */}
                <td>{bet.marketName}</td>

                {/* Player */}
                <td>{bet.customerId}</td>

                {/* Predicted Result */}
                <td>{getResultText(bet.result)}</td>

                {/* Bet Status */}
                <td>{bet.status}</td>

                {/* Bet Won */}
                <td>{getBetWon(bet)}</td>

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

    if (votesCount !== null) {
        return (
            <div style={{ padding: '1rem', border: '1px solid #ccc', borderRadius: '8px', backgroundColor: '#f9f9f9' }}>
                <p style={{ marginBottom: '0.5rem', color: '#555' }}>
                    <strong>Bet List is not available:</strong> Market is not closed yet.
                </p>
                <p>Total votes so far: <strong>{votesCount}</strong></p>
            </div>
        );
    }

    return (
        <div>
            <h4>List of Bets for Movie Event "{event.item1} vs {event.item2}"</h4>
            <Link href={`/bets/place/${eventId}`}>
                <button className="btn btn-primary" style={{ marginBottom: '10px' }}>Place a Bet</button>
            </Link>
            <table className="table">
                <thead>
                <tr>
                    <th>Movie Event</th>
                    <th>Player</th>
                    <th>Predicted Result</th>
                    <th>Bet Status</th>
                    <th>Bet Won</th>
                    <th>View Bet</th>
                </tr>
                </thead>
                <tbody>{betList}</tbody>
            </table>
        </div>
    );
};

export default withAuth(BetListPage);