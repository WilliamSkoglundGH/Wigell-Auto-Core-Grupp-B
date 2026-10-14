package com.wac.autocore.model.enums;

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
        public boolean canCancel(){
            return true;
        }

        @Override
        public boolean canConfirm(){
            return true;
        }

        @Override
        public boolean canEdit(){
            return true;
        }

        @Override
        public WorkOrderState getNext() {
            return CONFIRMED;
        }
    },

    CONFIRMED() {
        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean canComplete() {
            return false;
        }

        @Override
        public boolean canCancel(){
            return false;
        }

        @Override
        public boolean canConfirm(){
            return false;
        }

        @Override
        public boolean canEdit(){
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
        public boolean canCancel(){
            return false;
        }

        @Override
        public boolean canConfirm(){
            return false;
        }

        @Override
        public boolean canEdit(){
            return false;
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
        public boolean canCancel(){
            return false;
        }

        @Override
        public boolean canConfirm(){
            return false;
        }

        @Override
        public boolean canEdit(){
            return false;
        }

        @Override
        public WorkOrderState getNext() {
            throw new WorkOrderWrongStatusException("No more states after completed.");
        }
    },

    CANCELED() {
        @Override
        public boolean canStart() {
            return false;
        }

        @Override
        public boolean canComplete() {
            return false;
        }

        @Override
        public boolean canCancel(){
            return false;
        }

        @Override
        public boolean canConfirm(){
            return false;
        }

        @Override
        public boolean canEdit(){
            return false;
        }

        @Override
        public WorkOrderState getNext() {
            throw new WorkOrderWrongStatusException("No more states after cancellation.");
        }
    };

    public abstract boolean canStart();
    public abstract boolean canComplete();
    public abstract boolean canCancel();
    public abstract boolean canConfirm();
    public abstract boolean canEdit();
    public abstract WorkOrderState getNext();
}