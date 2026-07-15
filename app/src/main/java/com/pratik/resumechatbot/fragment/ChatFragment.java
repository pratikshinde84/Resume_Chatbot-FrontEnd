package com.pratik.resumechatbot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pratik.resumechatbot.R;
import com.pratik.resumechatbot.adapter.ChatAdapter;
import com.pratik.resumechatbot.viewmodel.MainViewModel;

public class ChatFragment extends Fragment {
    private MainViewModel viewModel;
    private ChatAdapter adapter;
    private EditText etQuery;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        etQuery = view.findViewById(R.id.et_query);
        progressBar = view.findViewById(R.id.chat_progress);
        RecyclerView recyclerView = view.findViewById(R.id.rv_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_send).setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                viewModel.sendQuery(query);
                etQuery.setText("");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.setMessages(messages);
            if (adapter.getItemCount() > 0) {
                ((RecyclerView)view.findViewById(R.id.rv_chat)).scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}
