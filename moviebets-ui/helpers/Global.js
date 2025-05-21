export const getResultText = (result) => {
    switch (result) {
        case "ITEM1_WINS":
            return "Movie 1 Wins";
        case "ITEM2_WINS":
            return "Movie 2 Wins";
        default:
            return "Unknown";
    }
};

export const getEventResultText = (open, result) => {
    if (open) {
        return "OPEN";
    }
    return getResultText(result);
};

export const getBetWon = (bet) => {
    switch (bet.status) {
        case "SETTLED":
            return bet.betWon ? '✅' : '❌'
        case "CANCELLED":
            return '🚫'
        default:
            return '⏳'
    }
};