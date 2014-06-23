package com.partymanager.activity.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.partymanager.R;
import com.partymanager.data.Adapter.AttributiAdapter;
import com.partymanager.data.Adapter.FbFriendsAdapter;
import com.partymanager.data.Adapter.FriendsAdapter;
import com.partymanager.data.Adapter.RisposteAdapter;
import com.partymanager.data.DatiAttributi;
import com.partymanager.data.DatiEventi;
import com.partymanager.data.DatiFriends;
import com.partymanager.data.DatiRisposte;
import com.partymanager.data.Friends;
import com.partymanager.helper.DataProvide;
import com.partymanager.helper.EventDialog;
import com.partymanager.helper.HelperConnessione;
import com.partymanager.helper.HelperFacebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evento extends Fragment {

    // <editor-fold defaultstate="collapsed" desc="Variabili Globali">
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    private static String idEvento;
    private String nomeEvento;
    private String adminEvento;
    private String numUtenti;
    private TextView bnt_friends;
    boolean animation;
    static RisposteAdapter adapter;

    AttributiAdapter eAdapter;
    ListView listView;
    View riepilogo;
    EventDialog eventDialog;
    static TextView luogo;
    static TextView quando_data;
    TextView quando_ora;
    static TextView dove;
    int mLastFirstVisibleItem = 0;
    Dialog dialog;
    EditText edt;
    Dialog dialogAddDomanda;
    Dialog dialogFriends;
    ProgressBar pb;
    ArrayList<Friends> friendList;
    FbFriendsAdapter dataAdapter = null;
    ArrayList<Friends> friendsList;
    List<GraphUser> friends;
    EditText inputSearch;
    ListView amiciFB;
    Dialog dialogAddFriends;
    ArrayList<String> id_toSend;
    WebDialog f;
    ProgressDialog progressDialog;

    private static final int DIALOG_DATA = 1;
    private static final int DIALOG_ORARIO_E = 2;
    private static final int DIALOG_ORARIO_I = 3;
    private static final int DIALOG_LUOGO_I = 4;
    private static final int DIALOG_PERSONALLIZATA = 5;
    private static final int DIALOG_LUOGO_E = 6;
    private static final int DIALOG_SINO = 7;

    private OnFragmentInteractionListener mListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init + Grafica">
    public static Evento newInstance(String param1, String param2, String param3, String param4) {
        Evento fragment = new Evento();

        Log.e("Evento newInstance: ", "id: " + param1 + " nome: " + param2 + " admin: " + param3 + " #utenti: " + param4);

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        fragment.setArguments(args);

        return fragment;
    }

    public Evento() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            idEvento = getArguments().getString(ARG_PARAM1);
            nomeEvento = getArguments().getString(ARG_PARAM2);
            adminEvento = getArguments().getString(ARG_PARAM3);
            numUtenti = getArguments().getString(ARG_PARAM4);
        }

        eventDialog = new EventDialog(getActivity(), dialogMsgHandler, idEvento, adminEvento);
        eAdapter = DatiAttributi.init(getActivity(), idEvento, Integer.parseInt(numUtenti));

        dialogAddDomanda = eventDialog.returnD();
    }

    public static void checkTemplate() {
        ArrayList<DatiAttributi.Attributo> prova = DatiAttributi.ITEMS;

        for (DatiAttributi.Attributo temp : prova) {
            //Log.e("checkTEmplate-TEST: ", temp.id + " " + temp.domanda + " " + temp.risposta + " " + temp.template + " " + temp.close);
            if (temp.template.equals("data")) {
                quando_data.setText(temp.risposta);
            }
            if (temp.template.equals("luogoE")) {
                luogo.setText(temp.risposta);
            }
            if (temp.template.equals("luogoI")) {
                dove.setText(temp.risposta);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evento, container, false);

        listView = (ListView) view.findViewById(R.id.eventList);
        riepilogo = view.findViewById(R.id.stickyheader);
        bnt_friends = (TextView) view.findViewById(R.id.imgButton_amici);
        luogo = (TextView) view.findViewById(R.id.txt_luogo);
        quando_data = (TextView) view.findViewById(R.id.txt_data);
        quando_ora = (TextView) view.findViewById(R.id.txt_orario);
        dove = (TextView) view.findViewById(R.id.txt_dove_vediamo);

        final View add_domanda = view.findViewById(R.id.circle);

        bnt_friends.setText(numUtenti);

        luogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.which(2);
                dialogAddDomanda.show();
            }
        });

        quando_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.which(3);
                dialogAddDomanda.show();
            }
        });

        quando_ora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.which(5);
                dialogAddDomanda.show();
            }
        });

        dove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.which(1);
                dialogAddDomanda.show();
            }
        });

        add_domanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.which(0);
                dialogAddDomanda.show();
            }
        });

        bnt_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogEventUsers();
            }
        });

        // <editor-fold defaultstate="collapsed" desc="listView">
        listView.setEmptyView(view.findViewById(R.id.txt_empty));
        listView.setAdapter(eAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                    long arg3) {
                dialogRisposte(arg2);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                Log.e("long clicked", "pos: " + pos);

                if (adminEvento.equals(HelperFacebook.getFacebookId())) {
                    PopupMenu popup = new PopupMenu(getActivity(), arg1);
                    popup.getMenuInflater().inflate(R.menu.popup_delete, popup.getMenu());
                    popup.show();
                }

                return true;
            }
        });

        animation = false;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > mLastFirstVisibleItem && totalItemCount > 9) {
                    if (!animation) {
                        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, +2 * add_domanda.getWidth());
                        anim.setDuration(500);
                        anim.setFillAfter(true);
                        add_domanda.startAnimation(anim);
                        animation = true;
                    }
                }
                if (firstVisibleItem < mLastFirstVisibleItem || listView.getLastVisiblePosition() == totalItemCount - 1) {
                    if (animation) {
                        TranslateAnimation anim = new TranslateAnimation(0, 0, +2 * add_domanda.getWidth(), 0);
                        anim.setDuration(500);
                        anim.setFillAfter(true);
                        add_domanda.startAnimation(anim);
                        animation = false;
                    }
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
        // </editor-fold>

        return view;
    }

    private void sendInviti(String temp) {
        f = HelperFacebook.inviteFriends(getActivity(), temp);
        f.show();
        f.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogAddFriends.dismiss();
            }
        });
    }

    private void addFriendsToEvent(final String List, final int quanti_aggiunti) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getActivity().getApplicationContext().getString(R.string.aggiuntaAmico));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... args) {
                String ris;

                ris = HelperConnessione.httpPostConnection("friends/" + idEvento, new String[]{"userList"}, new String[]{List});

                Log.e("Evento-addFriendsToEvent-ris: ", ris);

                return ris;
            }

            @Override
            protected void onPostExecute(String result) {
                id_toSend.clear();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                if (!result.equals("fatto")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage(getString(R.string.errAggAmico));

                    alertDialogBuilder.setPositiveButton(getString(R.string.chiudi), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    dialogFriends.dismiss();
                    FbFriendsAdapter.svuotaLista();

                    for (int i = 0; i < DatiEventi.ITEMS.size(); i++) {
                        if (DatiEventi.ITEMS.get(i).id == Integer.parseInt(idEvento)) {
                            DatiEventi.ITEMS.get(i).numUtenti += quanti_aggiunti;
                            bnt_friends.setText(numUtenti + quanti_aggiunti);
                            break;
                        }
                    }
                }
            }
        }.execute();
    }

    private void eliminaUser(final int i, final ProgressBar pb_buttaFuori) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... args) {
                String ris;

                ris = HelperConnessione.httpDeleteConnection("friends/" + idEvento + "/" + DatiFriends.ITEMS.get(i).getCode());

                Log.e("Evento-eliminaUser-ris: ", ris);

                return ris;
            }

            @Override
            protected void onPostExecute(String result) {
                pb_buttaFuori.setVisibility(View.GONE);
                if (!result.equals("fatto")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage(getString(R.string.errDeleteFriend));

                    alertDialogBuilder.setPositiveButton(getString(R.string.chiudi), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    DatiFriends.removeItem(i);

                    for (int i = 0; i < DatiEventi.ITEMS.size(); i++) {
                        if (DatiEventi.ITEMS.get(i).id == Integer.parseInt(idEvento)) {
                            DatiEventi.ITEMS.get(i).numUtenti--;
                            bnt_friends.setText("" + DatiEventi.ITEMS.get(i).numUtenti);
                            break;
                        }
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DatiAttributi.removeAll();
        eAdapter.notifyDataSetChanged();
        if (dialog != null)
            dialog.dismiss();
        if (eventDialog != null)
            eventDialog.close();
        if (dialogFriends != null)
            dialogFriends.dismiss();
        if (dialogAddFriends != null)
            dialogAddFriends.dismiss();
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Richiesta amici FB">
    private void requestMyAppFacebookFriends(Session session) {
        Request friendsRequest = createRequest(session);
        friendsRequest.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                friends = getResults(response);
                friendsList = new ArrayList<Friends>();

                for (GraphUser user : friends) {
                    //controllo chi ha l'app installata
                    Boolean install = false;
                    if (user.getProperty("installed") != null && user.getProperty("installed").toString().equals("true")) {
                        install = true;
                    }

                    Friends friend = new Friends(user.getId(), user.getName(), false, install);
                    friendsList.add(friend);
                }

                pb.setVisibility(ProgressBar.GONE);

                dataAdapter = new FbFriendsAdapter(getActivity().getApplicationContext(), null, inputSearch, R.layout.fb_friends, friendsList);
                amiciFB.setAdapter(dataAdapter);
                dataAdapter.setAdapter(dataAdapter);
                friendList = dataAdapter.friendList;
            }
        });
        friendsRequest.executeAsync();
    }

    private Request createRequest(Session session) {
        Request request = Request.newGraphPathRequest(session, "me/friends", null);

        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[]{"id", "name", "installed"};
        fields.addAll(Arrays.asList(requiredFields));

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }

    private List<GraphUser> getResults(Response response) {
        GraphMultiResult multiResult = response
                .getGraphObjectAs(GraphMultiResult.class);
        GraphObjectList<GraphObject> data = multiResult.getData();
        return data.castToListOf(GraphUser.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="dialogRisposte">
    public void dialogRisposte(final int arg2) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_risposte);

        final ListView risp = (ListView) dialog.findViewById(R.id.listView_risposte);
        adapter = DatiRisposte.init(getActivity().getApplicationContext(), idEvento, DatiAttributi.ITEMS.get(arg2).id, Integer.parseInt(numUtenti), arg2);
        risp.setAdapter(adapter);

        TextView text = (TextView) dialog.findViewById(R.id.txt_domanda_dialog);
        text.setText(DatiAttributi.ITEMS.get(arg2).domanda);

        final ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.imgBSend);
        edt = (EditText) dialog.findViewById(R.id.edtxt_nuovaRisposta);

        if (DatiAttributi.ITEMS.get(arg2).close) {
            edt.setVisibility(View.GONE);
            dialogButton.setVisibility(View.GONE);
        } else {
            edt.setHint("Scrivi qui la tua risposta");
        }

        final ProgressBar pb_add = (ProgressBar) dialog.findViewById(R.id.pb_addRisposta);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(edt.getText().toString())) {
                    dialogButton.setVisibility(View.GONE);
                    pb_add.setVisibility(View.VISIBLE);
                    addRisposta(DatiAttributi.ITEMS.get(arg2).id, edt.getText().toString(), DatiAttributi.ITEMS.get(arg2).template, pb_add, dialogButton);
                }
            }
        });

        if (DatiAttributi.ITEMS.get(arg2).template != null) {
            if (DatiAttributi.ITEMS.get(arg2).template.equals("sino")) {
                LinearLayout normal = (LinearLayout) dialog.findViewById(R.id.risposta_stringa);
                normal.setVisibility(View.GONE);
                LinearLayout sino = (LinearLayout) dialog.findViewById(R.id.linearL_sino);
                sino.setVisibility(View.VISIBLE);

                final ProgressBar pb_sino = (ProgressBar) dialog.findViewById(R.id.pb_sino);

                Button no = (Button) dialog.findViewById(R.id.btn_risp_no);
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pb_sino.setVisibility(View.VISIBLE);
                        addDomandaSino("no", pb_sino, arg2);
                    }
                });

                Button si = (Button) dialog.findViewById(R.id.btn_risp_si);
                si.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pb_sino.setVisibility(View.VISIBLE);
                        addDomandaSino("si", pb_sino, arg2);
                    }
                });

                if (DatiAttributi.ITEMS.get(arg2).close) {
                    si.setVisibility(View.GONE);
                    no.setVisibility(View.GONE);
                }
            }
            if (DatiAttributi.ITEMS.get(arg2).template.equals("data")) {
                LinearLayout normal = (LinearLayout) dialog.findViewById(R.id.risposta_stringa);
                normal.setVisibility(View.GONE);
                LinearLayout dataL = (LinearLayout) dialog.findViewById(R.id.linearL_data);
                dataL.setVisibility(View.VISIBLE);

                final ProgressBar pb_data = (ProgressBar) dialog.findViewById(R.id.pb_data);

                final DatePicker dateR = (DatePicker) dialog.findViewById(R.id.datePicker_risposta);

                Button add = (Button) dialog.findViewById(R.id.button_rispndi_data);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String temp = Integer.toString(dateR.getDayOfMonth()) + "/" + Integer.toString(dateR.getMonth() + 1) + "/" + Integer.toString(dateR.getYear());
                        pb_data.setVisibility(View.VISIBLE);
                        addRisposta(DatiAttributi.ITEMS.get(arg2).id, temp, "data", pb_data, null);
                    }
                });

                if (DatiAttributi.ITEMS.get(arg2).close) {
                    add.setVisibility(View.GONE);
                    dateR.setVisibility(View.GONE);
                }
            }
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                try {
                    DatiRisposte.removeAll(true, idEvento, DatiAttributi.ITEMS.get(arg2).id);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("Evento-dialog.setOnDismissListener", "IndexOutOfBoundsException " + e);
                }
            }
        });

        dialog.show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="dialogEventUsers">
    public void dialogEventUsers() {
        DataProvide.getFriends(idEvento, getActivity().getApplicationContext());
        dialogFriends = new Dialog(getActivity());
        dialogFriends.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFriends.setContentView(R.layout.dialog_friends);

        pb = (ProgressBar) dialogFriends.findViewById(R.id.progressBar_addFriends);
        pb.setVisibility(View.VISIBLE);

        ListView utenti = (ListView) dialogFriends.findViewById(R.id.listView_friends);
        FriendsAdapter adapter = DatiFriends.init(idEvento, getActivity().getApplicationContext());
        utenti.setEmptyView(pb);
        utenti.setAdapter(adapter);

        dialogFriends.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatiFriends.removeAll();
            }
        });

        Button addFriends = (Button) dialogFriends.findViewById(R.id.btn_addFriends);

        final ProgressBar pb_buttaFuori = (ProgressBar) dialogFriends.findViewById(R.id.pb_deleteUser);

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddFriends();
            }
        });

        utenti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (adminEvento.equals(HelperFacebook.getFacebookId())) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    popup.getMenuInflater().inflate(R.menu.popup_butta_fuori, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(android.view.MenuItem item) {
                            pb_buttaFuori.setVisibility(View.VISIBLE);
                            eliminaUser(i, pb_buttaFuori);
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        });

        dialogFriends.show();
    }
    // </editor-fold">

    // <editor-fold defaultstate="collapsed" desc="dialogAddFriends">
    public void dialogAddFriends() {
        requestMyAppFacebookFriends(HelperFacebook.getSession(getActivity()));
        dialogAddFriends = new Dialog(getActivity());
        dialogAddFriends.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAddFriends.setContentView(R.layout.dialog_friends);
        dialogAddFriends.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        pb = (ProgressBar) dialogAddFriends.findViewById(R.id.progressBar_addFriends);
        pb.setVisibility(View.VISIBLE);

        inputSearch = (EditText) dialogAddFriends.findViewById(R.id.editText_search_dialog_friends);
        inputSearch.setVisibility(View.VISIBLE);

        amiciFB = (ListView) dialogAddFriends.findViewById(R.id.listView_friends);

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                int textlength = cs.length();
                ArrayList<Friends> tempArrayList = new ArrayList<Friends>();
                if (friendList != null && friendList.size() > 0) {
                    for (Friends friends1 : friendList) {
                        if (textlength <= friends1.getName().length()) {
                            if (friends1.getName().toLowerCase().contains(cs.toString().toLowerCase())) {
                                tempArrayList.add(friends1);
                            }
                        }
                    }
                    dataAdapter = new FbFriendsAdapter(getActivity().getApplicationContext(), null, inputSearch, R.layout.fb_friends, tempArrayList);
                    amiciFB.setAdapter(dataAdapter);
                    dataAdapter.setAdapter(dataAdapter);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        Button addFriends = (Button) dialogAddFriends.findViewById(R.id.btn_addFriends);
        addFriends.setText(getString(R.string.addFriends));
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id_toSend = new ArrayList<String>();

                StringBuilder id_to_invite = new StringBuilder();
                List<Friends> finali = dataAdapter.getFinali();

                for (Friends aFinali : finali) {
                    if (aFinali.getAppInstalled()) {
                        id_toSend.add(aFinali.getCode());
                    } else {
                        String temp = aFinali.getCode();
                        if (id_to_invite.length() != 0)
                            temp = ", " + temp;

                        id_to_invite.append(temp);
                    }
                }
                JSONArray jsArray = new JSONArray(id_toSend);

                Log.e("Evento-AddFriends-Persone prima di invio: ", jsArray.toString());
                addFriendsToEvent(jsArray.toString(), jsArray.length());
                if (!id_to_invite.toString().equals(""))
                    sendInviti(id_to_invite.toString());

                dialogAddFriends.dismiss();
            }
        });

        dialogAddFriends.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (id_toSend != null)
                    id_toSend.clear();
            }
        });

        dialogAddFriends.show();

    }
    // </editor-fold">

    // <editor-fold defaultstate="collapsed" desc="addRisposte + Vota">
    private void addRisposta(final String id_attributo, final String risposta, final String template, final ProgressBar pb_add, final ImageButton dialogButton) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                if (dialogFriends != null && dialogFriends.isShowing())
                    dialogFriends.dismiss();
            }

            @Override
            protected String doInBackground(Void... params) {

                String[] name, param;
                name = new String[]{"risposta"};
                param = new String[]{risposta};
                String ris = HelperConnessione.httpPostConnection("event/" + idEvento + "/" + id_attributo, name, param);

                Log.e("addRisposta-ris: ", ris);

                return ris;
            }

            @Override
            protected void onPostExecute(String ris) {
                pb_add.setVisibility(View.GONE);

                if (dialogButton != null)
                    dialogButton.setVisibility(View.VISIBLE);

                if (isInteger(ris)) {
                    JSONObject pers = new JSONObject();
                    JSONArray userL = new JSONArray();
                    try {
                        pers.put("id_user", HelperFacebook.getFacebookId());
                        pers.put("name", HelperFacebook.getFacebookUserName());
                        userL.put(pers);
                        cercami();
                        DatiRisposte.addItem(new DatiRisposte.Risposta(ris, risposta, template, userL));
                        edt.setText("");
                    } catch (JSONException e) {
                        Log.e("Evento-addRisposta", "JSONException " + e);
                    }
                }
            }

            private boolean isInteger(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }

        }.execute(null, null, null);
    }

    public static void vota(final Button vota, final String idRisposta, final int position, final ProgressBar pb_vota) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                pb_vota.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {
                String[] name, param;

                name = new String[]{"idRisposta"};
                param = new String[]{idRisposta};

                return HelperConnessione.httpPutConnection("event/" + idEvento + "/" + DatiAttributi.ITEMS.get(adapter.getId()).id, name, param);
            }

            @Override
            protected void onPostExecute(String ris) {
                pb_vota.setVisibility(View.GONE);
                if (vota != null)
                    vota.setVisibility(View.VISIBLE);

                Log.e("Evento-vota-ris:", ris);

                if (ris.equals("aggiornato")) {
                    graficaVota(position, adapter.getId());
                }
            }
        }.execute(null, null, null);
    }

    public static void graficaVota(int position, int attuale) {
        cercami();

        if (DatiRisposte.ITEMS.size() > position) //serve come controllo di sicurezza ma non dovrebbe mai capitare
            DatiRisposte.ITEMS.get(position).addPersona(new DatiRisposte.Persona(HelperFacebook.getFacebookId(), HelperFacebook.getFacebookUserName()));

        int temp = 0;
        String risposta_max = null, idMax = null;

        for (int i = 0; i < DatiRisposte.ITEMS.size(); i++) {
            if (DatiRisposte.ITEMS.get(i).persone.size() > temp) {
                temp = DatiRisposte.ITEMS.get(i).persone.size();
                idMax = DatiRisposte.ITEMS.get(i).id;
                risposta_max = DatiRisposte.ITEMS.get(i).risposta;
            }
        }
        DatiAttributi.ITEMS.get(attuale).changeRisposta(risposta_max, idMax);
    }

    private static void cercami() {
        Boolean trovato = false;
        for (int i = 0; i < DatiRisposte.ITEMS.size() && !trovato; i++) {
            for (int j = 0; DatiRisposte.ITEMS.get(i).persone != null && j < DatiRisposte.ITEMS.get(i).persone.size() && !trovato; j++) {
                if (DatiRisposte.ITEMS.get(i).persone.get(j).id_fb.equals(HelperFacebook.getFacebookId())) {
                    DatiRisposte.ITEMS.get(i).persone.remove(j);
                    trovato = true;
                }
            }
        }
    }

    public void addDomandaSino(String cosa, ProgressBar pb_sino, int attuale) {

        if (DatiRisposte.ITEMS.size() == 1) {
            addRisposta(DatiAttributi.ITEMS.get(attuale).id, cosa, "sino", pb_sino, null);
        } else {
            if (cosa.equals("si"))
                vota(null, DatiRisposte.ITEMS.get(0).id, 0, pb_sino);
            else {
                vota(null, DatiRisposte.ITEMS.get(1).id, 1, pb_sino);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Handler">
    private Handler dialogMsgHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String ris;
            if (msg != null) {
                int who = msg.getData().getInt("who");
                boolean close = msg.getData().getBoolean("close");
                String id_attributo = msg.getData().getString("id_attributo");
                String ris2;
                switch (who) {
                    case DIALOG_DATA:
                        ris = msg.getData().getString("data");
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, "Data Evento", ris, "data", close, 1, 1, null));
                        break;
                    case DIALOG_ORARIO_E:
                        ris = msg.getData().getString("orario");
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, "Orario Evento", ris, null, close, 1, 1, null));
                        break;
                    case DIALOG_ORARIO_I:
                        ris = msg.getData().getString("orario");
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, "Orario Incontro", ris, null, close, 1, 1, null));
                        break;
                    case DIALOG_LUOGO_I:
                        ris = msg.getData().getString("luogo");
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, "Luogo incontro", ris, "luogoI", close, 1, 1, null));
                        break;
                    case DIALOG_LUOGO_E:
                        ris = msg.getData().getString("luogo");
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, "Luogo Evento", ris, "luogoE", close, 1, 1, null));
                        break;
                    case DIALOG_PERSONALLIZATA:
                        ris = msg.getData().getString("pers-d");
                        ris2 = "";
                        if (close) {
                            ris2 = msg.getData().getString("pers-r");
                        }
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, ris, ris2, null, close, 1, 1, null));
                        break;
                    case DIALOG_SINO:
                        ris = msg.getData().getString("domanda");
                        ris2 = "1 voto: 100% SI";
                        DatiAttributi.addItem(new DatiAttributi.Attributo(id_attributo, ris, ris2, null, false, 1, 1, null));
                        break;
                }
            }
        }
    };
    // </editor-fold>
}