package com.wac.autocore.model.enums;

//TODO har bara lagt in något för att få det att kompilera!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!





public enum WorkOrderType {
    PLANNED("Planerad arbetsorder", "Planned work order"),
    DROP_IN("Drop-in-arbetsorder", "Drop-in work order"),
    CLAIM("Reklamation", "Claim work order");

    private final String swedishName;
    private final String englishName;

    WorkOrderType(String swedishName, String englishName) {
        this.swedishName = swedishName;
        this.englishName = englishName;
    }

    public String getSwedishName() {
        return swedishName;
    }

    public String getEnglishName() {
        return englishName;
    }

    // Smidig metod om man vill visa rätt namn beroende på valt språk (eller använd ResourceBundle om ni föredrar det)
    public String getDisplayName(boolean isSwedish) {
        return isSwedish ? swedishName : englishName;
    }
}