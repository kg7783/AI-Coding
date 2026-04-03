package de.einmaleins.trainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    
    private List<ProgressSession> sessions;
    private OnSessionDeleteListener deleteListener;

    public interface OnSessionDeleteListener {
        void onDelete(String sessionId);
    }

    public SessionAdapter(List<ProgressSession> sessions, OnSessionDeleteListener listener) {
        this.sessions = sessions != null ? sessions : new ArrayList<>();
        this.deleteListener = listener;
    }

    public void updateSessions(List<ProgressSession> newSessions) {
        this.sessions = newSessions != null ? newSessions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProgressSession session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvCorrect;
        private final TextView tvWrong;
        private final TextView tvAccuracy;
        private final ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCorrect = itemView.findViewById(R.id.tvCorrect);
            tvWrong = itemView.findViewById(R.id.tvWrong);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ProgressSession session) {
            String startTime = DATE_FORMAT.format(new Date(session.getStartTimestamp()));
            String endTime = session.getEndTimestamp() > 0 
                    ? DATE_FORMAT.format(new Date(session.getEndTimestamp()))
                    : "...";
            tvDate.setText(startTime + " - " + endTime);
            
            tvCorrect.setText(String.format(Locale.GERMAN, "%d", session.getCorrectAnswers()));
            tvWrong.setText(String.format(Locale.GERMAN, "%d", session.getWrongAnswers()));
            tvAccuracy.setText(String.format(Locale.GERMAN, "%.0f%%", session.getAccuracy()));

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(session.getId());
                }
            });
        }
    }
}
