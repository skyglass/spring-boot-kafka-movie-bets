import Link from 'next/link';
import { useEffect, useState } from 'react';
import Router from 'next/router';
import { useKeycloak } from '../../auth/provider/KeycloakProvider';
import buildClient from '../../api/build-client';
import { useRouter } from 'next/router';
import { useMessage } from "../../provider/MessageContextProvider";

const PlayerShow = () => {
    const { user } = useKeycloak();
    const router = useRouter();
    const { playerId } = router.query;
    const [player, setPlayer] = useState(null);
    const { showMessage } = useMessage();

    useEffect(() => {
        if (user && playerId) {
            const fetchPlayer = async () => {
                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/customer/get-customer/${playerId}`);
                    setPlayer(data);
                } catch (error) {
                    const errorMsg =
                        error.response?.data?.message ||
                        error.message ||
                        "Unexpected error fetching player.";
                    showMessage(errorMsg, 'error');
                }
            };
            fetchPlayer();
        }
    }, [user, playerId]);

    if (!player) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>Player {player.customerId}</h1>

            <h3>Player Information</h3>
            <div className="market-data">
                <div>
                    <p><strong>Balance:</strong> {player.balance}</p>
                </div>
            </div>

            <Link href={`/bets/view/player/${playerId}`}>
                <button className="btn btn-primary" style={{ marginBottom: '10px' }}>View Bets</button>
            </Link>
        </div>
    )
};

export default PlayerShow;