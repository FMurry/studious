package xyz.fmsoft.studious.Utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by fredericmurry on 12/30/16.
 */

public class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {



    public interface DateListener{
        void returnFormattedDate(String date, int key);
    }

    DateListener dateListener;
    int key;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        Bundle bundle = getArguments();
        key = bundle.getInt("key");
        dateListener = (DateListener)getActivity();

        return new DatePickerDialog(getActivity(),this,year,month,day);
    }


    /**
     * @param view        The view associated with this listener.
     * @param year        The year that was set.
     * @param monthOfYear The month that was set (0-11) for compatibility
     *                    with {@link Calendar}.
     * @param dayOfMonth  The day of the month that was set.
     */

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year,monthOfYear,dayOfMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(c.getTime());
        if (dateListener != null){
            dateListener.returnFormattedDate(date,key);
        }
    }


}
