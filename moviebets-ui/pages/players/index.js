import Link from 'next/link';
import { useEffect, useState } from "react";
import withAuth from "../../auth/middleware/withAuth";
import buildClient from "../../api/build-client";
import { useKeycloak } from "../../auth/provider/KeycloakProvider";
import { v4 as uuidv4 } from 'uuid';
import {useMessage} from "../../provider/MessageContextProvider";

const PlayerList = () => {
  const [players, setPlayers] = useState([]);
  const { user } = useKeycloak();
  const { showMessage } = useMessage();

  useEffect(() => {
    if (user) {
      const fetchPlayers = async () => {
        try {
          const client = buildClient({ req: {}, currentUser: user });
          const { data } = await client.get("/api/customer/all");
          setPlayers(data);

          const existingPlayer = data.find(p => p.customerId === user.name);

          if (!existingPlayer) {
            const requestId = uuidv4();
            await client.post(`/api/customer/register/${user.name}/${requestId}`);

            // Refetch updated list
            const { data: updatedData } = await client.get("/api/customer/all");
            setPlayers(updatedData);
          }
        } catch (error) {
          const errorMsg =
              error.response?.data?.message ||
              error.message ||
              "Unexpected error fetching players.";
          showMessage(errorMsg, 'error');
        }
      };
      fetchPlayers();
    }
  }, [user]);

  if (!players) {
    return <div>Loading...</div>;
  }

  return (
      <div>
        <h4>Players</h4>
        <table className="table">
          <thead>
          <tr>
            <th>Username</th>
            <th>Balance</th>
            <th>Action</th>
          </tr>
          </thead>
          <tbody>
          {players.map(player => (
              <tr key={player.customerId}>
                <td>{player.customerId}</td>
                <td>{player.balance}</td>
                <td>
                  <Link href={`/bets/view/player/${player.customerId}`}>
                    View Bets
                  </Link>
                </td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
  );
};

export default withAuth(PlayerList);