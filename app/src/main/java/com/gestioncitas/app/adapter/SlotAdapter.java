package com.gestioncitas.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gestioncitas.app.R;
import com.gestioncitas.app.model.Slot;

import java.util.List;

/** Adapter para la lista de horarios (slots) disponibles. */
public class SlotAdapter extends ArrayAdapter<Slot> {

    private final LayoutInflater inflater;

    public SlotAdapter(Context context, List<Slot> slots) {
        super(context, 0, slots);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_slot, parent, false);
            holder = new ViewHolder();
            holder.hora = convertView.findViewById(R.id.textoHora);
            holder.estado = convertView.findViewById(R.id.textoEstado);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Slot slot = getItem(position);
        if (slot != null) {
            holder.hora.setText(slot.getHora());
            if (slot.isOcupado()) {
                holder.estado.setText(R.string.slot_ocupado);
                holder.estado.setTextColor(Color.parseColor("#C62828"));
                holder.hora.setTextColor(Color.parseColor("#9E9E9E"));
            } else {
                holder.estado.setText(R.string.slot_disponible);
                holder.estado.setTextColor(Color.parseColor("#2E7D32"));
                holder.hora.setTextColor(Color.parseColor("#212121"));
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView hora;
        TextView estado;
    }
}
