package com.wac.autocore.model;

import com.wac.autocore.exception.WorkOrderWrongStatusException;

public enum WorkOrderState {

    DRAFT() {
        @Override
        public boolean canStart() {
            return false;
        }

        @Override
        public boolean canComplete() {
            return false;
        }

        @Override
        public WorkOrderState getNext() {
            return CREATED;
        }
    },

    CREATED() {
        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean canComplete() {
            return false;
        }
        @Override
        public WorkOrderState getNext() {
            return IN_PROGRESS;
        }
    },

    IN_PROGRESS() {
        @Override
        public boolean canStart() {
            return false;
        }

        @Override
        public boolean canComplete() {
            return true;
        }
        @Override
        public WorkOrderState getNext() { return COMPLETED; }
    },

    COMPLETED() {
        @Override
        public boolean canStart() {
            return false;
        }

        @Override
        public boolean canComplete() {
            return false;
        }
        @Override
        public WorkOrderState getNext() {
            throw new WorkOrderWrongStatusException("No more states after completed.");
        }
    };

    public abstract boolean canStart();
    public abstract boolean canComplete();
    public abstract WorkOrderState getNext();
}