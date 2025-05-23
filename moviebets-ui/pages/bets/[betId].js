import Link from 'next/link';
import { useEffect, useState } from 'react';
import Router from 'next/router';
import { useKeycloak } from '../../auth/provider/KeycloakProvider';
import buildClient from '../../api/build-client';
import { useRouter } from 'next/router';
import { getResultText, getBetWon } from "../../helpers/Global";
import { useMessage } from "../../provider/MessageContextProvider";

const BetShow = () => {
    const { user } = useKeycloak();
    const router = useRouter();
    const { betId } = router.query;
    const [bet, setBet] = useState(null);
    const { showMessage } = useMessage();

    useEffect(() => {
        if (user && betId) {
            const fetchBet = async () => {
                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/bet/get-state/${betId}`);
                    setBet(data);
                } catch (error) {
                    const errorMsg =
                        error.response?.data?.message ||
                        error.message ||
                        "Unexpected error fetching bets.";
                    showMessage(errorMsg, 'error');
                }
            };
            fetchBet();
        }
    }, [user, betId]);

    if (!bet) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h5>Bet for Movie Event: "{bet.marketName}"</h5>

            <div className="bet-data">
                <div>
                    <p><strong>Customer:</strong> {bet.customerId}</p>
                    <p><strong>Movie Event:</strong> {bet.marketName}</p>
                    <p><strong>Bet Prediction:</strong> {getResultText(bet.result)}</p>
                    <p><strong>Bet Status:</strong> {bet.status}</p>
                    <p><strong>Bet Won:</strong> {getBetWon(bet)}</p>
                </div>
            </div>

            <Link href={`/bets/view/event/${bet.marketId}`}>
            <button className="btn btn-primary" style={{ marginBottom: '10px' }}>View all bets for this movie event</button>
            </Link>
        </div>
    )
};

export default BetShow;