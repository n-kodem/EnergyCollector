package com.n_kodemandsniapa.energycollector


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_first.*
import java.io.File
import java.io.FileOutputStream


class FirstFragment : Fragment() {

    val FILENAME = "save.txt"

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
                    "$some\n$text"
                }
            }
            val parts = content?.split(":")

            locationView.text = content

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        super.onViewCreated(view, savedInstanceState)
        updateTextTask.run()


    }
}