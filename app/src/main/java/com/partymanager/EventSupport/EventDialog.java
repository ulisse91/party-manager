package com.partymanager.EventSupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.partymanager.R;
import com.partymanager.data.DatiAttributi;
import com.partymanager.helper.HelperConnessione;
import com.partymanager.helper.HelperFacebook;

import java.util.ArrayList;

public class EventDialog {

    Context context;
    Dialog dialog;
    EditText alto;
    EditText risposta;
    DatePicker date;
    Button close;
    CheckBox chiusura;
    TimePicker orario;
    Spinner sp;

    private int idEvento;
    private static final int DIALOG_DATA = 1;
    private static final int DIALOG_ORARIO_E = 2;
    private static final int DIALOG_ORARIO_I = 3;
    private static final int DIALOG_LUOGO_I = 4;
    private static final int DIALOG_PERSONALLIZATA = 5;
    private static final int DIALOG_LUOGO_E = 6;
    private static final int DIALOG_SINO = 7;
    private Handler mResponseHandler;
    ProgressDialog progressDialog;
    private boolean first;

    public EventDialog(final Context context, Handler reponseHandler, int idEvento, String adminEvento) {
        this.context = context;
        this.mResponseHandler = reponseHandler;
        this.idEvento = idEvento;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_domanda_2);
        alto = (EditText) dialog.findViewById(R.id.editText2);
        risposta = (EditText) dialog.findViewById(R.id.editText_risposta);
        date = (DatePicker) dialog.findViewById(R.id.datePicker);
        chiusura = (CheckBox) dialog.findViewById(R.id.cb_chiusura);
        close = (Button) dialog.findViewById(R.id.btn_close);
        orario = (TimePicker) dialog.findViewById(R.id.timePicker);

        if (!HelperFacebook.getFacebookId().equals(adminEvento)) {
            chiusura.setVisibility(View.GONE);
        } else {
            chiusura.setVisibility(View.VISIBLE);
        }

        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); //per tutto schermo
        dialog.setCanceledOnTouchOutside(true);

        sp = (Spinner) dialog.findViewById(R.id.spinner);
        renderSpinner();
        first = false;

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (first) {
                    String Text = sp.getSelectedItem().toString().replace(" ", "").replace("/", "");
                    which(Text, arg2);
                } else {
                    first = true;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public enum Template {
        DataEvento,
        OrarioEvento,
        OrarioIncontro,
        LuogoEvento,
        LuogoIncontro,
        Personalizzata,
        DomandaSINO,
    }

    public void which(String selectItem, int pos) {
        chiusura.setChecked(false);
        Template quale = Template.valueOf(selectItem);

        switch (quale) {
            case Personalizzata:
                personalizzata();
                break;
            case LuogoIncontro:
                luogoI();
                break;
            case LuogoEvento:
                luogoE();
                break;
            case DataEvento:
                date();
                break;
            case OrarioIncontro:
                orarioI();
                break;
            case OrarioEvento:
                orarioE();
                break;
            case DomandaSINO:
                domanda_chiusa();
                break;
        }

        sp.setSelection(pos);
    }

    public void renderSpinner() {

        String[] template = DatiAttributi.getTemplate();

        ArrayList<String> list = new ArrayList<String>();

        list.add(context.getString(R.string.pers));
        list.add(context.getString(R.string.dmndChiusa));
        list.add(context.getString(R.string.orarioI));

        if (template[2] == null)
            list.add(context.getString(R.string.luogoI));

        if (template[1] == null)
            list.add(context.getString(R.string.luogoE));

        if (template[0] == null)
            list.add(context.getString(R.string.dataE));

        if (template[1] == null)
            list.add(context.getString(R.string.orarioE));

        ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, list);

        sp.setAdapter(testAdapter);
    }

    public Dialog returnD() {
        //InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return dialog;
    }

    // <editor-fold defaultstate="collapsed" desc="Dialog Data">
    public void date() {

        dialog.setTitle(R.string.dataE);

        alto.setVisibility(View.GONE);
        risposta.setVisibility(View.GONE);
        date.setVisibility(View.VISIBLE);
        orario.setVisibility(View.GONE);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String temp = Integer.toString(date.getDayOfMonth()) + "/" + Integer.toString(date.getMonth() + 1) + "/" + Integer.toString(date.getYear());
                Log.e("DATASCELTA: ", temp);

                addDomanda(1, "Data Evento", idEvento, "data", temp);

                dialog.dismiss();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Orario Evento">
    public void orarioE() {

        dialog.setTitle(R.string.orarioE);

        alto.setVisibility(View.GONE);
        risposta.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.VISIBLE);

        orario.setIs24HourView(true);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String temp = Integer.toString(orario.getCurrentHour()) + ":" + Integer.toString(orario.getCurrentMinute());
                Log.e("ORARIOSCELTO: ", temp);

                addDomanda(2, "Orario Evento", idEvento, "oraE", temp);
                dialog.dismiss();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Orario Incontro">
    public void orarioI() {

        dialog.setTitle(R.string.orarioI);

        alto.setVisibility(View.GONE);
        risposta.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.VISIBLE);

        orario.setIs24HourView(true);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String temp = Integer.toString(orario.getCurrentHour()) + ":" + Integer.toString(orario.getCurrentMinute());
                Log.e("ORARIOSCELTO: ", temp);

                addDomanda(3, "Orario Incontro", idEvento, "oraI", temp);
                dialog.dismiss();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Luogo Evento">
    public void luogoE() {
        dialog.setTitle(R.string.luogoE);

        risposta.setText("");
        risposta.setHint(R.string.luogoEHint);

        alto.setVisibility(View.GONE);
        risposta.setVisibility(View.VISIBLE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.GONE);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("LUOGOSCELTO-E: ", risposta.getText().toString());

                if (!risposta.getText().toString().equals("")) {
                    addDomanda(4, "Luogo Evento", idEvento, "luogoE", risposta.getText().toString());
                }
                dialog.dismiss();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Luogo Incontro">
    public void luogoI() {
        dialog.setTitle(R.string.luogoI);

        risposta.setText("");
        risposta.setHint(R.string.luogoEHint);

        alto.setVisibility(View.GONE);
        risposta.setVisibility(View.VISIBLE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.GONE);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("LUOGOSCELTO-I: ", risposta.getText().toString());

                if (!risposta.getText().toString().equals("")) {
                    addDomanda(4, "Luogo Incontro", idEvento, "luogoI", risposta.getText().toString());
                }

                dialog.dismiss();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Personalizzata">
    public void personalizzata() {

        alto.setVisibility(View.VISIBLE);
        risposta.setVisibility(View.VISIBLE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.GONE);

        dialog.setTitle(R.string.pers);

        alto.setText("");
        risposta.setText("");
        alto.setHint(R.string.persDH);
        risposta.setHint(R.string.persRH);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("PERSONALIZZATA-DOMANDA: ", alto.getText().toString());
                Log.e("PERSONALIZZATA-RISPOSTA: ", risposta.getText().toString());

                if (!alto.getText().toString().equals("")) {
                    addDomanda(5, alto.getText().toString(), idEvento, "", risposta.getText().toString());
                }

                dialog.dismiss();

            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dialog Domanda Chiusa">
    public void domanda_chiusa() {
        dialog.setTitle(R.string.dmndChiusa);

        alto.setVisibility(View.VISIBLE);
        risposta.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        orario.setVisibility(View.GONE);

        alto.setHint(R.string.persDH);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.e("SI/NO: ", alto.getText().toString());
                if (!alto.getText().toString().equals("")) {
                    addDomanda(7, alto.getText().toString(), idEvento, "sino", "si");
                    dialog.dismiss();
                }
            }
        });
    }
    // </editor-fold>

    private void addDomanda(final int who, final String domanda, final int idEvento, final String template, final String risposta) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {

                InputMethodManager inputManager = (InputMethodManager)
                        context.getSystemService(Context.INPUT_METHOD_SERVICE);

                if (((Activity) context).getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(context.getString(R.string.creazDom));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {

                String chiusa = String.valueOf(chiusura.isChecked() ? 1 : 0);
                String[] name, param;

                if (template.equals("")) {
                    name = new String[]{"domanda", "risposta", "chiusa"};
                    param = new String[]{domanda, risposta, chiusa};
                } else {
                    name = new String[]{"domanda", "template", "risposta", "chiusa"};
                    param = new String[]{domanda, template, risposta, chiusa};
                }

                String ris = HelperConnessione.httpPostConnection("event/" + idEvento, name, param);

                Log.e("addDomanda-ris: ", ris);

                return ris;
            }

            @Override
            protected void onPostExecute(String ris) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                try {
                    int ris_temp = Integer.parseInt(ris);

                    Message m = new Message();
                    Bundle b = new Bundle();

                    switch (who) {
                        case DIALOG_DATA:
                            b.putInt("who", 1);
                            b.putString("data", risposta);
                            break;
                        case DIALOG_ORARIO_E:
                            b.putInt("who", 2);
                            b.putString("orario", risposta);
                            break;
                        case DIALOG_ORARIO_I:
                            b.putInt("who", 3);
                            b.putString("orario", risposta);
                            break;
                        case DIALOG_LUOGO_I:
                            b.putInt("who", 4);
                            b.putString("luogo", risposta);
                            break;
                        case DIALOG_LUOGO_E:
                            b.putInt("who", 6);
                            b.putString("luogo", risposta);
                            break;
                        case DIALOG_PERSONALLIZATA:
                            b.putInt("who", 5);
                            b.putString("pers-d", domanda);
                            b.putString("pers-r", risposta);
                            break;
                        case DIALOG_SINO:
                            b.putInt("who", 7);
                            b.putString("domanda", domanda);
                            break;
                    }

                    b.putBoolean("close", chiusura.isChecked());
                    b.putInt("id_attributo", ris_temp);
                    m.setData(b);
                    mResponseHandler.sendMessage(m);

                } catch (NumberFormatException e) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage(R.string.problInsDom);

                    alertDialogBuilder.setPositiveButton(R.string.chiudi, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        }.execute(null, null, null);
    }
}
