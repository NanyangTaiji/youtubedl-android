package com.yausername.youtubedl_android_example;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDLResponse;
import org.jetbrains.annotations.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

public class UnifiedDownloadActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Components
    private Button btnCommand, btnDownload, btnStream;
    private LinearLayout commandLayout, downloadLayout, streamLayout;
    
    // Command Mode UI
    private Button btnRunCommand, btnStopCommand;
    private EditText etCommand;
    private ProgressBar commandProgressBar;
    private TextView tvCommandStatus, tvCommandOutput;
    
    // Download Mode UI
    private Button btnStartDownload, btnStopDownload;
    private EditText etDownloadUrl;
    private Switch useConfigFile;
    private ProgressBar downloadProgressBar;
    private TextView tvDownloadStatus, tvDownloadOutput;
    
    // Stream Mode UI
    private Button btnStartStream;
    private EditText etStreamUrl;
    private VideoView videoView;
    
    // Common UI
    private ProgressBar pbLoading;
    private NestedScrollView scrollView;
    
    // State variables
    private boolean commandRunning = false;
    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String commandProcessId = "CommandProcess";
    private final String downloadProcessId = "DownloadProcess";
    
    private static final String TAG = UnifiedDownloadActivity.class.getSimpleName();
    
    // Command progress callback - using Runnable for UI updates
    private final DownloadProgressCallback commandCallback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds, @Nullable String status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commandProgressBar.setProgress((int) progress);
                    tvCommandStatus.setText(status);
                }
            });
        }
    };
    
    // Download progress callback
    private final DownloadProgressCallback downloadCallback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds, @Nullable String status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgressBar.setProgress((int) progress);
                    tvDownloadStatus.setText(status);
                }
            });
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_download);

        initViews();
        initListeners();
        
        // Default to download mode
        setActiveMode("download");
    }
    
    private void initViews() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("YoutubeDl");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Mode buttons
        btnCommand = findViewById(R.id.btn_command_mode);
        btnDownload = findViewById(R.id.btn_download_mode);
        btnStream = findViewById(R.id.btn_stream_mode);
        
        // Layouts
        commandLayout = findViewById(R.id.command_layout);
        downloadLayout = findViewById(R.id.download_layout);
        streamLayout = findViewById(R.id.stream_layout);
        scrollView = findViewById(R.id.scroll_view);
        pbLoading = findViewById(R.id.pb_status);
        
        // Command mode views
        btnRunCommand = findViewById(R.id.btn_run_command);
        btnStopCommand = findViewById(R.id.btn_stop_command);
        etCommand = findViewById(R.id.et_command);
        commandProgressBar = findViewById(R.id.command_progress_bar);
        tvCommandStatus = findViewById(R.id.tv_command_status);
        tvCommandOutput = findViewById(R.id.tv_command_output);
        
        // Download mode views
        btnStartDownload = findViewById(R.id.btn_start_download);
        btnStopDownload = findViewById(R.id.btn_stop_download);
        etDownloadUrl = findViewById(R.id.et_download_url);
        useConfigFile = findViewById(R.id.use_config_file);
        downloadProgressBar = findViewById(R.id.download_progress_bar);
        tvDownloadStatus = findViewById(R.id.tv_download_status);
        tvDownloadOutput = findViewById(R.id.tv_download_output);
        
        // Stream mode views
        btnStartStream = findViewById(R.id.btn_start_stream);
        etStreamUrl = findViewById(R.id.et_stream_url);
        videoView = findViewById(R.id.video_view);
        
        // Set initial visibility
        commandLayout.setVisibility(View.GONE);
        downloadLayout.setVisibility(View.GONE);
        streamLayout.setVisibility(View.GONE);
    }
    
    private void initListeners() {
        btnCommand.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnStream.setOnClickListener(this);
        
        btnRunCommand.setOnClickListener(this);
        btnStopCommand.setOnClickListener(this);
        btnStartDownload.setOnClickListener(this);
        btnStopDownload.setOnClickListener(this);
        btnStartStream.setOnClickListener(this);
        
        videoView.setOnPreparedListener(new com.devbrackets.android.exomedia.listener.OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoView.start();
            }
        });
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_command_mode) {
            setActiveMode("command");
        } else if (id == R.id.btn_download_mode) {
            setActiveMode("download");
        } else if (id == R.id.btn_stream_mode) {
            setActiveMode("stream");
        } else if (id == R.id.btn_run_command) {
            runCommand();
        } else if (id == R.id.btn_stop_command) {
            stopCommand();
        } else if (id == R.id.btn_start_download) {
            startDownload();
        } else if (id == R.id.btn_stop_download) {
            stopDownload();
        } else if (id == R.id.btn_start_stream) {
            startStream();
        }
    }
    
    private void setActiveMode(String mode) {
        // Reset button styles
        btnCommand.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        btnDownload.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        btnStream.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        
        // Hide all layouts
        commandLayout.setVisibility(View.GONE);
        downloadLayout.setVisibility(View.GONE);
        streamLayout.setVisibility(View.GONE);
        
        // Show selected layout and highlight button
        if (mode.equals("command")) {
            commandLayout.setVisibility(View.VISIBLE);
            btnCommand.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (mode.equals("download")) {
            downloadLayout.setVisibility(View.VISIBLE);
            btnDownload.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (mode.equals("stream")) {
            streamLayout.setVisibility(View.VISIBLE);
            btnStream.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
        
        scrollView.smoothScrollTo(0, 0);
    }
    
    private void runCommand() {
        if (commandRunning) {
            Toast.makeText(this, "Command already in progress", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (!isStoragePermissionGranted()) {
            Toast.makeText(this, "Grant storage permission and retry", Toast.LENGTH_LONG).show();
            return;
        }
        
        final String command = etCommand.getText().toString().trim();
        if (TextUtils.isEmpty(command)) {
            etCommand.setError(getString(R.string.command_error));
            return;
        }
        
        YoutubeDLRequest request = new YoutubeDLRequest(Collections.emptyList());
        String commandRegex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(commandRegex).matcher(command);
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1));
            } else {
                request.addOption(m.group(2));
            }
        }
        
        showCommandStart();
        commandRunning = true;

        Disposable disposable = Observable.fromCallable(new java.util.concurrent.Callable<YoutubeDLResponse>() {
                    @Override
                    public YoutubeDLResponse call() throws Exception {
                        return YoutubeDL.getInstance().execute(request, downloadProcessId);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<YoutubeDLResponse>() {
                    @Override
                    public void accept(YoutubeDLResponse response) throws Exception {
                        pbLoading.setVisibility(View.GONE);
                        commandProgressBar.setProgress(100);
                        tvCommandStatus.setText("Command completed");
                        tvCommandOutput.setText(response.getOut());
                        Toast.makeText(UnifiedDownloadActivity.this, "Command successful", Toast.LENGTH_LONG).show();
                        commandRunning = false;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        Log.e(TAG, "Command failed", e);
                        pbLoading.setVisibility(View.GONE);
                        tvCommandStatus.setText("Command failed");
                        tvCommandOutput.setText(e.getMessage());
                        Toast.makeText(UnifiedDownloadActivity.this, "Command failed", Toast.LENGTH_LONG).show();
                        commandRunning = false;
                    }
                });
        
        compositeDisposable.add(disposable);
    }
    
    private void stopCommand() {
        if (commandRunning) {
            try {
                YoutubeDL.getInstance().destroyProcessById(commandProcessId);
                commandRunning = false;
                Toast.makeText(this, "Command stopped", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop command", e);
            }
        }
    }
    
    private void startDownload() {
        if (downloading) {
            Toast.makeText(this, "Download already in progress", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (!isStoragePermissionGranted()) {
            Toast.makeText(this, "Grant storage permission and retry", Toast.LENGTH_LONG).show();
            return;
        }
        
        final String url = etDownloadUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etDownloadUrl.setError(getString(R.string.url_error));
            return;
        }
        
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        File config = new File(youtubeDLDir, "config.txt");
        
        if (useConfigFile.isChecked() && config.exists()) {
            request.addOption("--config-location", config.getAbsolutePath());
        } else {
            request.addOption("--no-mtime");
            request.addOption("--downloader", "libaria2c.so");
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        }
        
        showDownloadStart();
        downloading = true;

        Disposable disposable = Observable.fromCallable(new java.util.concurrent.Callable<YoutubeDLResponse>() {
                    @Override
                    public YoutubeDLResponse call() throws Exception {
                        return YoutubeDL.getInstance().execute(request, downloadProcessId);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<YoutubeDLResponse>() {
                    @Override
                    public void accept(YoutubeDLResponse response) throws Exception {
                        pbLoading.setVisibility(View.GONE);
                        downloadProgressBar.setProgress(100);
                        tvDownloadStatus.setText(getString(R.string.download_complete));
                        tvDownloadOutput.setText(response.getOut());
                        Toast.makeText(UnifiedDownloadActivity.this, "Download successful", Toast.LENGTH_LONG).show();
                        downloading = false;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        Log.e(TAG, "Failed to download", e);
                        pbLoading.setVisibility(View.GONE);
                        tvDownloadStatus.setText(getString(R.string.download_failed));
                        tvDownloadOutput.setText(e.getMessage());
                        Toast.makeText(UnifiedDownloadActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                        downloading = false;
                    }
                });
        
        compositeDisposable.add(disposable);
    }
    
    private void stopDownload() {
        if (downloading) {
            try {
                YoutubeDL.getInstance().destroyProcessById(downloadProcessId);
                downloading = false;
                Toast.makeText(this, "Download stopped", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop download", e);
            }
        }
    }
    
    private void startStream() {
        final String url = etStreamUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etStreamUrl.setError(getString(R.string.url_error));
            return;
        }
        
        pbLoading.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(new java.util.concurrent.Callable<VideoInfo>() {
                    @Override
                    public VideoInfo call() throws Exception {
                        YoutubeDLRequest request = new YoutubeDLRequest(url);
                        request.addOption("-f", "best");
                        return YoutubeDL.getInstance().getInfo(request);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VideoInfo>() {
                    @Override
                    public void accept(VideoInfo streamInfo) throws Exception {
                        pbLoading.setVisibility(View.GONE);
                        String videoUrl = streamInfo.getUrl();
                        if (TextUtils.isEmpty(videoUrl)) {
                            Toast.makeText(UnifiedDownloadActivity.this, "Failed to get stream URL", Toast.LENGTH_LONG).show();
                        } else {
                            videoView.setMedia(Uri.parse(videoUrl));
                            videoView.start();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        Log.e(TAG, "Failed to get stream info", e);
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(UnifiedDownloadActivity.this, "Streaming failed", Toast.LENGTH_LONG).show();
                    }
                });
        
        compositeDisposable.add(disposable);
    }
    
    private void showCommandStart() {
        tvCommandStatus.setText("Command starting...");
        commandProgressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
        tvCommandOutput.setText("");
    }
    
    private void showDownloadStart() {
        tvDownloadStatus.setText(getString(R.string.download_start));
        downloadProgressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
        tvDownloadOutput.setText("");
    }
    
    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
        if (!youtubeDLDir.exists()) {
            youtubeDLDir.mkdirs();
        }
        return youtubeDLDir;
    }
    
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }
    
    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_upgrade) {
            updateYoutubeDL();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateYoutubeDL() {
        pbLoading.setVisibility(View.VISIBLE);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update Channel")
                .setItems(new String[]{"Stable Releases", "Nightly Releases", "Master Releases"},
                        (dialogInterface, which) -> {
                            if (which == 0)
                                updateYoutubeDL(YoutubeDL.UpdateChannel._STABLE);
                            else if (which == 1)
                                updateYoutubeDL(YoutubeDL.UpdateChannel._NIGHTLY);
                            else
                                updateYoutubeDL(YoutubeDL.UpdateChannel._MASTER);
                        })
                .create();
        dialog.show();
    }


    private boolean updating = false;

    private void updateYoutubeDL(YoutubeDL.UpdateChannel updateChannel) {
        if (updating) {
            Toast.makeText(this, "Update is already in progress!", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
        pbLoading.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(this, updateChannel))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                   // pbLoading.setVisibility(View.GONE);
                    switch (status) {
                        case DONE:
                            Toast.makeText(this, "Update successful " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            Toast.makeText(this, "Already up to date " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(this, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                    updating = false;
                }, e -> {
                    Log.e(TAG, "failed to update", e);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "update failed", Toast.LENGTH_LONG).show();
                    updating = false;
                });
        compositeDisposable.add(disposable);
    }

}