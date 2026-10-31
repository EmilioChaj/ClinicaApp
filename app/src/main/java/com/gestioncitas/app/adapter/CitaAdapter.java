package com.gestioncitas.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gestioncitas.app.R;
import com.gestioncitas.app.model.Cita;

import java.util.List;

/**
 * Adapter para las citas del paciente.
 * Muestra médico, especialidad, fecha, hora, motivo y estado.
 * El botón de cancelar solo se muestra para citas futuras programadas.
 */
public class CitaAdapter extends ArrayAdapter<Cita> {

    public interface OnCancelarClickListener {
        void onCancelarClick(Cita cita);
    }

    private final LayoutInflater inflater;
    private final OnCancelarClickListener listener;
    private final boolean mostrarBotonCancelar;

    public CitaAdapter(Context context, List<Cita> citas, boolean mostrarBotonCancelar,
                       OnCancelarClickListener listener) {
        super(context, 0, citas);
        this.inflater = LayoutInflater.from(context);
        this.mostrarBotonCancelar = mostrarBotonCancelar;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_cita, parent, false);
            holder = new ViewHolder();
            holder.medico = convertView.findViewById(R.id.textoMedico);
            holder.especialidad = convertView.findViewById(R.id.textoEspecialidad);
            holder.fechaHora = convertView.findViewById(R.id.textoFechaHora);
            holder.motivo = convertView.findViewById(R.id.textoMotivo);
            holder.estado = convertView.findViewById(R.id.textoEstado);
            holder.botonCancelar = convertView.findViewById(R.id.botonCancelar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Cita cita = getItem(position);
        if (cita != null) {
            holder.medico.setText(cita.getMedicoNombre());
            holder.especialidad.setText(cita.getEspecialidad());
            holder.fechaHora.setText(cita.getFecha() + "  •  " + cita.getHora());
            holder.motivo.setText(cita.getMotivo());

            if (cita.esCancelada()) {
                holder.estado.setText(R.string.estado_cancelada);
                holder.estado.setTextColor(Color.parseColor("#C62828"));
            } else {
                holder.estado.setText(R.string.estado_programada);
                holder.estado.setTextColor(Color.parseColor("#2E7D32"));
            }

            if (mostrarBotonCancelar && cita.esProgramada()) {
                holder.botonCancelar.setVisibility(View.VISIBLE);
                holder.botonCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onCancelarClick(cita);
                        }
                    }
                });
            } else {
                holder.botonCancelar.setVisibility(View.GONE);
                holder.botonCancelar.setOnClickListener(null);
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView medico;
        TextView especialidad;
        TextView fechaHora;
        TextView motivo;
        TextView estado;
        Button botonCancelar;
    }
}
