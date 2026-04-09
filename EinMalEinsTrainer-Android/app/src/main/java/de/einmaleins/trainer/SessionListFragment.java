package de.einmaleins.trainer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SessionListFragment extends Fragment implements SessionAdapter.OnSessionDeleteListener {
    private RecyclerView recyclerView;
    private SessionAdapter adapter;
    private SessionManager sessionManager;
    private LinearLayout emptyState;
    private ImageButton btnClearAll;

    public static SessionListFragment newInstance() {
        return new SessionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSessions();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyState = view.findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SessionAdapter(sessionManager.loadSessions(), this);
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    public void setupClearAllButton(ImageButton button) {
        btnClearAll = button;
        btnClearAll.setOnClickListener(v -> showClearAllDialog());
        updateEmptyState();
    }

    public void updateSessions() {
        adapter.updateSessions(sessionManager.loadSessions());
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (btnClearAll != null) {
            btnClearAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDelete(String sessionId) {
        sessionManager.deleteSession(sessionId);
        adapter.updateSessions(sessionManager.loadSessions());
        updateEmptyState();
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_alle_loeschen_title)
                .setMessage(R.string.dialog_alle_loeschen_message)
                .setPositiveButton(R.string.btn_loeschen, (dialog, which) -> {
                    sessionManager.clearAllSessions();
                    adapter.updateSessions(sessionManager.loadSessions());
                    updateEmptyState();
                })
                .setNegativeButton(R.string.btn_abbrechen, null)
                .show();
    }
}
