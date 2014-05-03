package com.partymanager.app;

import android.content.Context;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.partymanager.R;
import com.partymanager.app.dummy.*;
import com.partymanager.app.helper.helperFacebook;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the callback
 * interface.
 */
public class EventiListFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static ProgressBar progressBarLarge;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView listView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private EventAdapter eAdapter;

    // TODO: Rename and change types of parameters
    public static EventiListFragment newInstance() {
        EventiListFragment fragment = new EventiListFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public EventiListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        // TODO: Change Adapter to display your content

        /*
        eAdapter = new EventAdapter (getActivity(), DatiEventi.ITEMS);
        DatiEventi.eAdapter = eAdapter; */
        String idFacebbok = helperFacebook.getFacebookId(getActivity());
        if (idFacebbok!= null)
            eAdapter = DatiEventi.init(getActivity(), idFacebbok);

        /*
        mAdapter = new ArrayAdapter<DatiEventi.Evento>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DatiEventi.ITEMS);
        */
       // ProgressBar progressBarLarge = (ProgressBar) getActivity().findViewById(R.id.eventProgressBarLarge);
        //ProgressBar progressBarSmall = (ProgressBar) getActivity().findViewById(R.id.progressBarSmall);

        //progressBarLarge.setVisibility(View.VISIBLE);
       // DataProvide.getEvent(getActivity(), progressBarLarge, progressBarSmall);

        //MainActivity.progressBarVisible = false;


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventilist_list, container, false);

        // Set the adapter
        listView = (ListView) view.findViewById(R.id.eventList);
        //((AdapterView<ListAdapter>) mListView).setAdapter(eAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);



        listView.setAdapter(eAdapter);


        //progressBarLarge.setVisibility(View.INVISIBLE);
        //getActivity().invalidateOptionsMenu();

        return view;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }

    }


    @Override
        public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.mTitle = "Eventi";

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            String idEvento = Integer.toString(DatiEventi.ITEMS.get(position).id);
            String name = DatiEventi.ITEMS.get(position).name;
            mListener.onFragmentInteraction(idEvento, name);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = listView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public static interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id, String name);
    }

}
