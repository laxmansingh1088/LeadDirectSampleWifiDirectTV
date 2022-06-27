package com.example.leadp2pdirect.servers;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.leadp2pdirect.P2PCallBacks;
import com.example.leadp2pdirect.constants.Constants;
import com.example.leadp2pdirect.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TransferData extends AsyncTask<Void, FileDownloadUploadProgresssModel, Void> {
    private Context context;
    private InetAddress serverAddress;
    private ArrayList<Uri> uris;
    private ContentResolver cr = null;
    private P2PCallBacks p2PCallBacks;
    private String timeTakenbyFile = "";

    private Long fileSize;
    private Long fileSizeOriginal;
    public File[] receivedFiles;
    private ArrayList<FileModel> receivedFilesPathList = new ArrayList<>();

    public TransferData(Context context,
                        ArrayList<Uri> uris, InetAddress serverAddress,
                        P2PCallBacks p2PCallBacks) {
        this.context = context;
        this.uris = uris;
        this.serverAddress = serverAddress;
        this.p2PCallBacks = p2PCallBacks;
    }


    private ContentResolver getContentResolverInstance() {
        if (cr == null) {
            cr = context.getContentResolver();
        }
        return cr;
    }

    private void sendData(Context context, ArrayList<Uri> uris) {
        int len = 0;
        byte buf[] = new byte[8192];

        Socket socket = new Socket();
        try {
            socket.bind(null);
            Log.d("Client Address", socket.getLocalSocketAddress().toString());
            socket.connect(new InetSocketAddress(serverAddress, Constants.FILE_TRANSFER_PORT));
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ArrayList<FileModel> fileModelArrayList = FileHelper.INSTANCE.getFileModelsListFromUris(uris, getContentResolverInstance());
            objectOutputStream.writeObject(fileModelArrayList);
            objectOutputStream.flush();

            for (int i = 0; i < uris.size(); i++) {
                InputStream inputStream = cr.openInputStream(uris.get(i));

                FileModel fileModel = fileModelArrayList.get(i);
                FileDownloadUploadProgresssModel progress = new FileDownloadUploadProgresssModel();
                progress.setFileName(fileModel.getFileName());
                progress.setDataIncrement(0);
                progress.setTotalProgress(0);
                progress.setSendingOrReceiving(FileDownloadUploadProgresssModel.SendingOrReceiving.Sending.name());
                progress.setFileLength(fileModel.getFileLength());

                long startTime = System.currentTimeMillis();
                while ((len = inputStream.read(buf)) != -1) {
                    objectOutputStream.write(buf, 0, len);
                    objectOutputStream.flush();
                    progress.setDataIncrement((long) len);
                    if (((int) (progress.getTotalProgress() * 100 / fileModel.getFileLength())) ==
                            ((int) ((progress.getTotalProgress() + progress.getDataIncrement()) * 100 / fileModel.getFileLength()))) {
                        progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                        continue;
                    }
                    progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                    publishProgress(progress);
                }
                inputStream.close();
                Log.d("TRANSFER", "Writing Data Final   -" + len);

                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;

                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
                long seconds = (TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60);
                timeTakenbyFile = "FileSize:- " + Utils.INSTANCE.humanReadableByteCountSI(fileModel.getFileLength()) + " &&  Time Taken:- " + minutes + " min : " + seconds + " sec";
                Log.d("Time--elapsed:->", timeTakenbyFile);
            }
            outputStream.close();
            objectOutputStream.close();
            socket.close();

        } catch (Exception e) {
            Log.d("Data Transfer", e.toString());
            e.printStackTrace();
        } finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


        @Override
        protected void onPreExecute () {
            super.onPreExecute();
            getContentResolverInstance();
        }

        @Override
        protected Void doInBackground (Void...params){
            sendData(context, uris);
            return null;
        }

        @Override
        protected void onProgressUpdate (FileDownloadUploadProgresssModel...values){
            super.onProgressUpdate(values);
            FileDownloadUploadProgresssModel fileDownloadProgresssModel = values[0];
            // Total progress
            int progress = (int) ((fileDownloadProgresssModel.getTotalProgress() * 100) / fileDownloadProgresssModel.getFileLength());
            fileDownloadProgresssModel.setProgressPercentage(progress);
            p2PCallBacks.onProgressUpdate(fileDownloadProgresssModel);
        }

        @Override
        protected void onPostExecute (Void aVoid){
            super.onPostExecute(aVoid);
            p2PCallBacks.timeTakenByFileToSend(timeTakenbyFile);
            Log.d("Sender", "Finished!");
        }

        @Override
        protected void onCancelled () {
            super.onCancelled();
        }

    }
