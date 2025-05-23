import Link from 'next/link';
import { useEffect, useState } from 'react';
import Router from 'next/router';
import { useKeycloak } from '../../auth/provider/KeycloakProvider';
import buildClient from '../../api/build-client';
import { useRouter } from 'next/router';
import { useMessage } from "../../provider/MessageContextProvider";

const EventShow = () => {
    const { user } = useKeycloak();
    const router = useRouter();
    const { eventId } = router.query;
    const [event, setEvent] = useState(null);
    const { showMessage } = useMessage();

    useEffect(() => {
        if (user && eventId) {
            const fetchEvent = async () => {
                try {
                    const client = buildClient({ req: {}, currentUser: user });
                    const { data } = await client.get(`/api/market/get-state/${eventId}`);
                    setEvent(data);
                } catch (error) {
                    const errorMsg =
                        error.response?.data?.message ||
                        error.message ||
                        "Unexpected error fetching event.";
                    showMessage(errorMsg, 'error');
                }
            };
            fetchEvent();
        }
    }, [user, eventId]);

    if (!event) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>{event.item1} vs {event.item2}</h1>

            <h3>Movie Event Information</h3>
            <div className="market-data">
                <div>
                    <p><strong>Movie 1:</strong> {event.item1}</p>
                    <p><strong>Movie 2:</strong> {event.item2}</p>
                </div>
                <div className="additional-info">
                    {event.closesAt && (
                        <p><strong>Close Time:</strong> {new Date(event.closesAt).toLocaleString()}</p>
                    )}
                </div>
            </div>

            <Link href={`/bets/place/${eventId}`}>
                <button className="btn btn-primary" style={{ marginBottom: '10px' }}>Place a Bet</button>
            </Link>
        </div>
    )
};

export default EventShow;