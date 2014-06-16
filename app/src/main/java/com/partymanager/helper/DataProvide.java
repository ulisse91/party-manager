package com.partymanager.helper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.partymanager.activity.MainActivity;
import com.partymanager.activity.fragment.Evento;
import com.partymanager.data.DatiAttributi;
import com.partymanager.data.DatiEventi;
import com.partymanager.data.DatiFriends;
import com.partymanager.data.DatiRisposte;
import com.partymanager.data.Friends;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataProvide {

    public static void getEvent(Context context) {
        loadJson("eventi", context);
        downloadEvent(context);
    }

    public static void getAttributi(Context context, String eventoId) {
        loadJson("attributi_" + eventoId, context);
        downloadAttributi(eventoId, context);
    }

    public static void getRisposte(String id_evento, String id_attr, Context context) {
        loadJson("risposte_" + id_evento + "_" + id_attr, context);
        downloadRisposte(id_evento, id_attr, context);
    }

    public static void getFriends(String idEvento, Context context) {
        loadJson("friends" + idEvento, context);
        downloadFriends(idEvento, context);
    }

    // <editor-fold defaultstate="collapsed" desc="download...">
    private static void downloadFriends(final String idEvento, final Context context) {
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected JSONArray doInBackground(Void... params) {
                String json_string = HelperConnessione.httpGetConnection("user/" + idEvento);
                return stringToJsonArray("user" , json_string);
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {

                if (jsonArray != null) {
                    saveJson(jsonArray, "friends" + idEvento, context);
                    loadIntoFriendsAdapter(jsonArray);
                }
            }
        }.execute(null, null, null);
    }

    private static void downloadEvent(final Context context) {
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected void onPreExecute() {
                MainActivity.progressBarVisible = true;
                ((Activity) context).invalidateOptionsMenu();
            }

            @Override
            protected JSONArray doInBackground(Void... params) {
                String json_string = HelperConnessione.httpGetConnection("event");
                return stringToJsonArray("event", json_string);
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {

                if (jsonArray != null) {
                    saveJson(jsonArray, "eventi", context);
                    loadIntoEventiAdapter(jsonArray);
                }

                MainActivity.progressBarVisible = false;
                ((Activity) context).invalidateOptionsMenu();

            }
        }.execute(null, null, null);
    }

    private static void downloadAttributi(final String id, final Context context) {
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected void onPreExecute() {
                MainActivity.progressBarVisible = true;
                ((Activity) context).invalidateOptionsMenu();

            }

            @Override
            protected JSONArray doInBackground(Void... params) {
                String jsonString = HelperConnessione.httpGetConnection("event/" + id);
                return stringToJsonArray("event/" + id, jsonString);
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {

                if (jsonArray != null) {
                    saveJson(jsonArray, "attributi_" + id, context);
                    loadIntoAttributiAdapter(jsonArray);
                }

                MainActivity.progressBarVisible = false;
                ((Activity) context).invalidateOptionsMenu();

                Evento.checkTemplate();
            }
        }.execute(null, null, null);
    }


    private static void downloadRisposte(final String id_evento, final String id_attr, final Context context) {
        new AsyncTask<Void, Void, JSONArray>() {

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected JSONArray doInBackground(Void... params) {
                String jsonString = HelperConnessione.httpGetConnection("event/" + id_evento + "/" + id_attr);
                return stringToJsonArray("event/" + id_evento + "/" + id_attr,jsonString);
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {
                if (jsonArray != null) {
                    saveJson(jsonArray, "risposte_" + id_evento + "_" + id_attr, context);
                    loadIntoRisposteAdapter(jsonArray);
                }
            }
        }.execute(null, null, null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="loadInto...Adapter">
    private static void loadIntoFriendsAdapter(JSONArray jsonArray) {
        DatiFriends.removeAll();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                DatiFriends.addItem(new Friends(jsonArray.getJSONObject(i).getString("id_user"),
                                jsonArray.getJSONObject(i).getString("name"), false, false)
                );
            }
        } catch (JSONException e) {
            Log.e("DataProvide", "JSONException loadIntoEventiAdapter: " + e);
        } catch (NullPointerException e) {
            Log.e("DataProvide", "NullPointerException loadIntoEventiAdapter: " + e);
        }
    }

    private static void loadIntoEventiAdapter(JSONArray jsonArray) {
        DatiEventi.removeAll();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                DatiEventi.addItem(new DatiEventi.Evento(
                        jsonArray.getJSONObject(i).getInt("id_evento"),
                        jsonArray.getJSONObject(i).getString("nome_evento"),
                        "content",
                        jsonArray.getJSONObject(i).getString("data"),
                        jsonArray.getJSONObject(i).getString("admin"),
                        jsonArray.getJSONObject(i).getInt("num_utenti")
                ));
            }
        } catch (JSONException e) {
            Log.e("DataProvide", "JSONException loadIntoEventiAdapter: " + e);
        } catch (NullPointerException e) {
            Log.e("DataProvide", "NullPointerException loadIntoEventiAdapter: " + e);
        }
    }

    private static void loadIntoAttributiAdapter(JSONArray jsonArray) {
        DatiAttributi.removeAll();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                DatiAttributi.addItem(new DatiAttributi.Attributo(
                        jsonArray.getJSONObject(i).getString("id_attributo"),
                        jsonArray.getJSONObject(i).getString("domanda"),
                        (jsonArray.getJSONObject(i).getString("risposta").equals("null")) ? "" : jsonArray.getJSONObject(i).getString("risposta"),
                        jsonArray.getJSONObject(i).getString("template"),
                        Boolean.valueOf(jsonArray.getJSONObject(i).getString("chiusa")),
                        (jsonArray.getJSONObject(i).getString("risposta").equals("null")) ? -1 : jsonArray.getJSONObject(i).getInt("numd"),
                        (jsonArray.getJSONObject(i).getString("risposta").equals("null")) ? -1 : jsonArray.getJSONObject(i).getInt("numr")
                ));
            }
            Evento.checkTemplate();
        } catch (JSONException e) {
            Log.e("DataProvide", "JSONException loadIntoAttributiAdapter: " + e);
        } catch (NullPointerException e) {
            Log.e("DataProvide", "NullPointerException loadIntoAttributiAdapter: " + e);
        }
    }

    private static void loadIntoRisposteAdapter(JSONArray jsonArray) {
        DatiRisposte.removeAll(false, null, null);
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                DatiRisposte.addItem(new DatiRisposte.Risposta(
                                String.valueOf(jsonArray.getJSONObject(i).getInt("id_risposta")),
                                jsonArray.getJSONObject(i).getString("risposta"),
                                jsonArray.getJSONObject(i).getString("template"),
                                jsonArray.getJSONObject(i).getJSONArray("userList")
                        )
                );
            }
        } catch (JSONException e) {
            Log.e("DataProvide", "JSONException loadIntoRisposteAdapter: " + e);
        } catch (NullPointerException e) {
            Log.e("DataProvide", "NullPointerException loadIntoRisposteAdapter: " + e);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metodi JSON">
    private static void loadJson(final String name, final Context context) {
        new AsyncTask<Void, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(Void... params) {

                return loadJsonFromFile(name, context);
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {
                if (jsonArray != null) {
                    if (name.equals("eventi"))
                        loadIntoEventiAdapter(jsonArray);
                    if (name.contains("attributi"))
                        loadIntoAttributiAdapter(jsonArray);
                    if (name.contains("risposte"))
                        loadIntoRisposteAdapter(jsonArray);
                    if (name.contains("friends"))
                        loadIntoFriendsAdapter(jsonArray);
                }
            }
        }.execute(null, null, null);
    }

    public static void saveJson(final JSONArray jsonArray, final String name, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                saveJsonToFile(jsonArray, name, context);
                return null;
            }

        }.execute(null, null, null);
    }

    public static void addElementJson(final JSONObject element, final String jsonName, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    JSONArray jsonArray = loadJsonFromFile(jsonName, context);
                    if (jsonArray != null) {
                        jsonArray = jsonArray.put(element);
                        saveJsonToFile(jsonArray, jsonName, context);
                    } else {
                        Log.e("DataProvide-addElementJson: ", "jsonArray == null");
                    }
                    return null;
                } catch (NullPointerException e) {
                    Log.e("DataProvide-addElementJson: ", "NullPointerException " + jsonName + " " + e);
                    return null;
                }
            }
        }.execute(null, null, null);
    }

    private static synchronized JSONArray loadJsonFromFile(String fileName, Context context) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return stringToJsonArrayBefore(sb.toString());

        } catch (IOException e) {
            Log.e("DATA_PROVIDE-loadJsonFromFile ", fileName + " " + e.toString());
        }
        return null;
    }

    private static synchronized void saveJsonToFile(JSONArray jsonArray, String fileName, Context context) {
        try {
            String jsonString = jsonArray.toString();
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("DataProvide", "IOException saveJsonToFile: " + fileName + " " + e);
        } catch (NullPointerException e) {
            Log.e("DataProvide", "NullPointerException saveJsonToFile: " + fileName + " " + e);
        }
    }

    private static JSONArray stringToJsonArray(String fileName, String jsonString) {

        try {
            JSONObject json_data = new JSONObject(jsonString);
            String status = json_data.getString("results");
            return new JSONArray(status);
        } catch (JSONException e) {
            Log.e("DataProvide-stringToJsonArray", "JSONException "+ fileName + " " + e);
            return null;
        }

    }

    private static JSONArray stringToJsonArrayBefore(String jsonString) {
        try {
            //JSONObject json_data = new JSONObject(jsonString);
            //String status = json_data.getString("results");
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e("DataProvide-stringToJsonArrayBefore", "JSONException " + e);
            return null;
        }
    }
    // </editor-fold>
}
