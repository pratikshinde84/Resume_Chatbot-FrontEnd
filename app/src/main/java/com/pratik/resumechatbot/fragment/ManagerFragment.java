package com.pratik.resumechatbot.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pratik.resumechatbot.R;
import com.pratik.resumechatbot.adapter.ResumeAdapter;
import com.pratik.resumechatbot.viewmodel.MainViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ManagerFragment extends Fragment {
    private MainViewModel viewModel;
    private ResumeAdapter adapter;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadFile(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        RecyclerView recyclerView = view.findViewById(R.id.rv_resumes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ResumeAdapter(filename -> viewModel.deleteResume(filename));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_upload).setOnClickListener(v -> filePickerLauncher.launch("application/pdf"));
        view.findViewById(R.id.btn_rebuild).setOnClickListener(v -> viewModel.rebuildIndex());
        view.findViewById(R.id.btn_sync).setOnClickListener(v -> viewModel.fetchResumes());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getResumes().observe(getViewLifecycleOwner(), resumes -> adapter.setResumes(resumes));
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.fetchResumes();
    }

    private void uploadFile(Uri uri) {
        try {
            File file = getFileFromUri(uri);
            if (file != null) {
                viewModel.uploadResume(file);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error preparing file", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        String fileName = getFileName(uri);
        File file = new File(requireContext().getCacheDir(), fileName);
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        return file;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }
}
