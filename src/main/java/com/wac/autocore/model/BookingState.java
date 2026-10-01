package com.wac.autocore.model;

public enum BookingState {
    BOOKED() {
         /*   @Override
            public boolean canStart() {
                return true;
            }

            @Override
            public boolean canComplete() {
                return false;
            }
            public BookingState getNext() { return WORK_ORDER_CREATED; }
            */
    },
    WORK_ORDER_CREATED() {},
    IN_PROGRESS() {},
    COMPLETED() {
        /*
        * @Override
        public BookingState getNext() {
            throw new IllegalStateException("No more states after completed.");
        }
        * */

    };

    // Abstrakta metoder för tillståndsregler
    //     public abstract boolean canStart();
    //     public abstract boolean canComplete();
    //public BookingState getNext();
}

