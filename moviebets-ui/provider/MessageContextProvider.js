import { createContext, useContext, useState } from 'react';

const MessageContext = createContext();

export const useMessage = () => useContext(MessageContext);

export const MessageContextProvider = ({ children }) => {
    const [message, setMessage] = useState(null); // { text: string, type: 'error' | 'success' }

    const showMessage = (text, type = 'info') => {
        setMessage({ text, type });
    };

    const clearMessage = () => setMessage(null);

    return (
        <MessageContext.Provider value={{ message, showMessage, clearMessage }}>
            {children}
        </MessageContext.Provider>
    );
};