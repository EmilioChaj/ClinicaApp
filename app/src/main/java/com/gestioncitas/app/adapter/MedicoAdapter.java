package com.gestioncitas.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gestioncitas.app.R;
import com.gestioncitas.app.model.Medico;

import java.util.List;

/** Adapter para la lista de médicos disponibles. */
public class MedicoAdapter extends ArrayAdapter<Medico> {

    private final LayoutInflater inflater;

    public MedicoAdapter(Context context, List<Medico> medicos) {
        super(context, 0, medicos);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_medico, parent, false);
            holder = new ViewHolder();
            holder.nombre = convertView.findViewById(R.id.textoNombreMedico);
            holder.especialidad = convertView.findViewById(R.id.textoEspecialidad);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Medico medico = getItem(position);
        if (medico != null) {
            holder.nombre.setText(medico.getNombre());
            holder.especialidad.setText(medico.getEspecialidad());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView nombre;
        TextView especialidad;
    }
}
