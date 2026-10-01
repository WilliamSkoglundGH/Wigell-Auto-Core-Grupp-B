package com.wac.autocore.model;

import com.wac.autocore.exception.BookingWrongStatusException;

public enum BookingState {
    BOOKED() {
        @Override
        public boolean canRemoveServiceItem() {
            return true;
        }

        @Override
        public boolean canAddServiceItem() {
            return true;
        }

        public BookingState getNext() {
            return WORK_ORDER_CREATED;
        }

    },
    WORK_ORDER_CREATED() {
        @Override
        public boolean canRemoveServiceItem() {
            return true;
        }

        @Override
        public boolean canAddServiceItem() {
            return true;
        }

        public BookingState getNext() {
            return IN_PROGRESS;
        }
    },
    IN_PROGRESS() {
        @Override
        public boolean canRemoveServiceItem() {
            return false;
        }

        @Override
        public boolean canAddServiceItem() {
            return false;
        }

        public BookingState getNext() {
            return COMPLETED;
        }
    },
    COMPLETED() {
        @Override
        public boolean canRemoveServiceItem() {
            return false;
        }

        @Override
        public boolean canAddServiceItem() {
            return false;
        }

        @Override
        public BookingState getNext() {
            throw new BookingWrongStatusException("No more states after completed.");
        }
    };

    public abstract boolean canRemoveServiceItem();

    public abstract boolean canAddServiceItem();

    public abstract BookingState getNext();

}

