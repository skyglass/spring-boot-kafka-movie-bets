import Link from 'next/link';
import withAuth from '../../../../auth/middleware/withAuth';
import buildClient from "../../../../api/build-client";
import { useKeycloak } from '../../../../auth/provider/KeycloakProvider';
import { useEffect, useState } from "react";
import { useRouter } from "next/router";
import {getBetWon, getResultText} from "../../../../helpers/Global";
import { useMessage } from "../../../../provider/MessageContextProvider";

const BetListPage = () => {
    const router = useRouter();
    const [bets, setBets] = useState([]);
    const { user } = useKeycloak();
    const { playerId } = router.query;
    const { showMessage } = useMessage();

    useEffect(() => {
        if (user) {
            const fetchData = async () => {

                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/bet/get-bets-for-player/${playerId}`);
                    setBets(data.betDataList);
                } catch (error) {
                    const errorMsg =
                        error.response?.data?.message ||
                        error.message ||
                        "Unexpected error fetching bets.";
                    showMessage(errorMsg, 'error');
                }
            };
            fetchData();
        }
    }, [user, playerId]);

    if (!bets) {
        return <div>Loading...</div>;
    }



    const betList = bets.map((bet) => {
        return (
            <tr key={bet.betId}>
                {/* Event */}
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

    return (
        <div>
            <h5>List of bets for player: {playerId}</h5>
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