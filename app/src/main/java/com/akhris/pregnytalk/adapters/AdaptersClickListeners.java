package com.akhris.pregnytalk.adapters;

public interface AdaptersClickListeners {

    /**
     * Common interface for RecyclerView's adapters
     */
    interface ItemClickListener {
        void onItemClick(int position);
    }


    /**
     * Interface for user clicks on user name showed on a message
     */
    interface MessageClickListener {
        void onUserNameClick(int position);
    }



    /**
     * Interface for contacts list item
     */
    interface ContactsItemClickListener {
        void onSendMessageClick(int position);
        void onItemClick(int position);
    }

}
