package com.n_kodemandsniapa.energycollector




import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_first.*
import java.io.File
import java.io.FileOutputStream


class FirstFragment : Fragment() {

    val FILENAME = "save.txt"
    lateinit var dataHandler: Handler

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)

    }

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {

            val SAVEFILE = File(context?.filesDir, FILENAME);


            var content = context?.openFileInput(FILENAME)?.bufferedReader()?.useLines { lines ->
                lines.fold("") { some, text ->
                    "$some$text"
                }
            }
            val parts = content?.split(":")

            //locationView.text = content
            if (parts != null) {
                if (parts.contains("true")||parts.contains("false"))
                    if (parts[3].toBoolean()){
                        //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }
            }
            view?.findViewById<TextView>(R.id.locationView)?.text= "Procent drogi ${(((parts?.get(0))?.toFloat())!! /1000.0)*100} \nLong: ${parts[1]} \nLat: ${parts[2]}\nDroga: ${parts[0]}\n" +
                    "przeładowanie: ${parts[3]}"

            view?.findViewById<ProgressBar>(R.id.progressBar)?.progress =((parts[0].toFloat()/1000.0)*100).toInt()
            view?.findViewById<TextView>(R.id.textProgress)?.text=((parts[0].toFloat()/1000.0)*100).toInt().toString()

            Log.d("","Procent drogi ${((parts[0].toFloat()) / 1000) * 100} \nLat: ${parts[1]} \nLong: ${parts[2]}\nDroga: ${parts[0]}\n" +
                    "                    \"przeładowanie: ${parts[3]}")
            dataHandler.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        this.dataHandler = Handler(Looper.getMainLooper())
        super.onViewCreated(view, savedInstanceState)
        updateTextTask.run()


    }
}