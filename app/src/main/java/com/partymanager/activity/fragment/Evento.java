package com.partymanager.activity.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.partymanager.R;
import com.partymanager.activity.EventDialog;
import com.partymanager.data.AttributiAdapter;
import com.partymanager.data.DatiAttributi;
import com.partymanager.data.DatiRisposte;
import com.partymanager.data.RisposteAdapter;
import com.partymanager.helper.HelperConnessione;
import com.partymanager.helper.HelperFacebook;

import java.util.ArrayList;

public class Evento extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    private String idEvento;
    private String nomeEvento;
    private String adminEvento;
    private String numUtenti;
    private ImageButton bnt_friends;
    boolean animation;

    AttributiAdapter eAdapter;
    ListView listView;
    View riepilogo;
    EventDialog eventDialog;
    static TextView luogo;
    static TextView quando_data;
    TextView quando_ora;
    static TextView dove;

    private static final int DIALOG_DATA = 1;
    private static final int DIALOG_ORARIO_E = 2;
    private static final int DIALOG_ORARIO_I = 3;
    private static final int DIALOG_LUOGO_I = 4;
    private static final int DIALOG_PERSONALLIZATA = 5;
    private static final int DIALOG_LUOGO_E = 6;
    private static final int DIALOG_SINO = 7;

    private OnFragmentInteractionListener mListener;

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
        eAdapter = DatiAttributi.init(getActivity(), idEvento);
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

    int mLastFirstVisibleItem = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evento, container, false);

        listView = (ListView) view.findViewById(R.id.eventList);
        riepilogo = view.findViewById(R.id.stickyheader);
        bnt_friends = (ImageButton) view.findViewById(R.id.imgButton_amici);
        luogo = (TextView) view.findViewById(R.id.txt_luogo);
        quando_data = (TextView) view.findViewById(R.id.txt_data);
        quando_ora = (TextView) view.findViewById(R.id.txt_orario);
        dove = (TextView) view.findViewById(R.id.txt_dove_vediamo);

        final View add_domanda = view.findViewById(R.id.circle);

        add_domanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventDialog.returnD().show();
            }
        });

        listView.setAdapter(eAdapter);

        bnt_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                                    long arg3) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_risposte);

                TextView text = (TextView) dialog.findViewById(R.id.txt_domanda_dialog);
                text.setText(DatiAttributi.ITEMS.get(arg2).domanda);

                ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.imgBSend);
                final EditText edt = (EditText) dialog.findViewById(R.id.edtxt_nuovaRisposta);
                edt.setHint("Scrivi qui la tua risposta");
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!"".equals(edt.getText().toString())) {
                            addRisposta(DatiAttributi.ITEMS.get(arg2).id, edt.getText().toString());
                        }

                        dialog.dismiss();
                    }
                });

                ListView risp = (ListView) dialog.findViewById(R.id.listView_risposte);
                RisposteAdapter adapter = DatiRisposte.init(getActivity().getApplicationContext(), idEvento, DatiAttributi.ITEMS.get(arg2).id);
                risp.setAdapter(adapter);

                dialog.show();
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
                if (firstVisibleItem > mLastFirstVisibleItem) {
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
        return view;
    }

    private void addRisposta(final String id_attributo, final String risposta) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {

                String[] name, param;

                name = new String[]{"risposta"};
                param = new String[]{risposta};

                String ris = HelperConnessione.httpPostConnection("http://androidpartymanager.herokuapp.com/event/" + idEvento + "/" + id_attributo, name, param);

                Log.e("addRisposta-ris: ", ris);

                return ris;
            }

            @Override
            protected void onPostExecute(String ris) {

            }
        }.execute(null, null, null);
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
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    private Handler dialogMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String ris;
            if (msg != null) {
                int who = msg.getData().getInt("who");
                boolean close = msg.getData().getBoolean("close");
                String ris2;
                switch (who) {
                    case DIALOG_DATA:
                        ris = msg.getData().getString("data");
                        Log.e("handler-DATA: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, "Data Evento", ris, "data", close, 1, 1));
                        break;
                    case DIALOG_ORARIO_E:
                        ris = msg.getData().getString("orario");
                        Log.e("handler-ORARIO-E: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, "Orario Evento", ris, null, close, 1, 1));
                        break;
                    case DIALOG_ORARIO_I:
                        ris = msg.getData().getString("orario");
                        Log.e("handler-ORARIO-I: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, "Orario Incontro", ris, null, close, 1, 1));
                        break;
                    case DIALOG_LUOGO_I:
                        ris = msg.getData().getString("luogo");
                        Log.e("handler-LUOGO-I: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, "Luogo incontro", ris, "luogoI", close, 1, 1));
                        break;
                    case DIALOG_LUOGO_E:
                        ris = msg.getData().getString("luogo");
                        Log.e("handler-LUOGO-E: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, "Luogo Evento", ris, "luogoE", close, 1, 1));
                        break;
                    case DIALOG_PERSONALLIZATA:
                        ris = msg.getData().getString("pers-d");
                        ris2 = "";
                        Log.e("handler-PERS: ", ris);
                        if (close) {
                            ris2 = msg.getData().getString("pers-r");
                        }
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, ris, ris2, null, close, 1, 1));
                        break;
                    case DIALOG_SINO:
                        ris = msg.getData().getString("domanda");
                        ris2 = "1 voto: 100% SI";
                        Log.e("handler-SINO: ", ris);
                        DatiAttributi.addItem(new DatiAttributi.Attributo(idEvento, ris, ris2, null, false, 1, 1));
                        break;
                }
            }
        }
    };
}