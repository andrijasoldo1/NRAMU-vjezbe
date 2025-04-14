package ba.sum.fsre.toplawv2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.toplawv2.models.DayModel;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final List<DayModel> days;
    private final OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(int day);
    }

    public CalendarAdapter(List<DayModel> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayModel dayModel = days.get(position);
        Context context = holder.itemView.getContext();

        if (dayModel.day == 0) {
            holder.dayText.setText("");
            holder.dotContainer.removeAllViews();
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.dayText.setText(String.valueOf(dayModel.day));
        holder.dayText.setAlpha(dayModel.isInCurrentMonth ? 1f : 0.4f);

        holder.dotContainer.removeAllViews();
        if (dayModel.dots != null) {
            for (int dotColor : dayModel.dots) {
                View dot = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
                params.setMargins(4, 0, 4, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundColor(dotColor);
                dot.setBackgroundResource(R.drawable.dot_circle);
                holder.dotContainer.addView(dot);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(dayModel.day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        LinearLayout dotContainer;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayNumberText);
            dotContainer = itemView.findViewById(R.id.dotContainer);
        }
    }
}
