package com.n_kodemandsniapa.energycollector

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.os.postDelayed
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.fragment_second.*
import java.lang.Math.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class SecondFragment : Fragment() {

    lateinit var mainHandler: Handler
    var ball_angle: Double = 0.toDouble()
    var game: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }


    private val updateBall = object : Runnable {
        @SuppressLint("SetTextI18n")


        override fun run() {

            val pad = view?.findViewById<ImageView>(R.id.imageView2)
            val circle = view?.findViewById<ImageView>(R.id.imageView)
            val ball = view?.findViewById<ImageView>(R.id.imageView4)

            if (ball != null && circle != null && pad != null) {


                if (game) {

                    val center_x = circle.x + circle.width / 2
                    val center_y = circle.y + circle.height / 2

                    val bal_rel_pos_x: Double = ball.x.toDouble() - center_x + ball.width / 2
                    val bal_rel_pos_y: Double = ball.y.toDouble() - center_y + ball.height / 2

                    val distance_from_center =
                        sqrt(pow(bal_rel_pos_x, 2.toDouble()) + pow(bal_rel_pos_y, 2.toDouble()))


                    if (distance_from_center > circle.width / 2 + ball.width * 2) {
                        scoreboard.text = "You Lost"
                        game = false
                        ball.visibility = View.INVISIBLE
                    }


                    ///im not sure if i have to subtract 360 like where the pad rotation is set. this [might] cause some problems.
                    //
                    //this code is a bit sussy but whatever. It works so im happy


                    val pad_angle = seekBar.progress * PI / 180

                    val ball_circle_angle: Double = atan2(bal_rel_pos_x, bal_rel_pos_y)
                    val ball_circle_new_angle: Double = ball_circle_angle - pad_angle

                    val transformed_x = sin(ball_circle_new_angle) * distance_from_center
                    val transformed_y = cos(ball_circle_new_angle) * distance_from_center

                    //Log.d("angle" , ball_circle_angle.toString())
                    //Log.d("new angle", ball_circle_new_angle.toString())
                    //Log.d("transformed" , transformed_x.toString() + " : " + transformed_y.toString())

                    //Log.d("seekBarAngle", seekBar.progress.toString())

                    if (
                        transformed_x < pad.width / 2 &&
                        transformed_x > -pad.width / 2 &&
                        transformed_y > circle.width / 2



                    ) {

                        var ball_angle2 = 0
                        //if(ball_angle - )

                        //the ball do be ballin and doesnt reflect correctly
                        Log.d("PING_angle_ball", ball_angle.toString())

                        // !!!!!!!!!!!!!!!!!!!!!
                        // Stopnie odbicia piki musza rosnac na bazie pad.width/5 i jak jest na 3 to odbija sie jak lustro na bazie poprzedniego wektora a jak dalej idzie to pod coraz wiekszym katem
                        // musisz po prostu sprawdzic o ktora z przykladowo 5 stfer sie odbija


                        ball_angle = ball_angle - ((ball_angle - pad_angle) - PI)*2 + pad_angle


                        Log.d("PING_angle_pad", pad_angle.toString())
                        Log.d("PING_angle_ball_new", ball_angle.toString())

                        ball.x = ball.x + 100 * (sin(ball_angle.toFloat()))
                        ball.y = ball.y + 100 * (cos(ball_angle.toFloat()))

                    } else {
                        ball.x = ball.x + 5 * (sin(ball_angle.toFloat()))
                        ball.y = ball.y + 5 * (cos(ball_angle.toFloat()))

                    }

                    //Log.d("current ball pos", bal_rel_pos_x.toString() + " : "+ bal_rel_pos_y .toString())


                }
                mainHandler.postDelayed(this, 1.toLong())
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val pad = view.findViewById<ImageView>(R.id.imageView2)
        val circle = view.findViewById<ImageView>(R.id.imageView)
        val ball = view.findViewById<ImageView>(R.id.imageView4)
        this.mainHandler = Handler(Looper.getMainLooper())

        super.onViewCreated(view, savedInstanceState)



        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }






        view.findViewById<SeekBar>(R.id.seekBar).max = 359

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val location = IntArray(2)

                pad.x =
                    (circle.width.toFloat() / 2 * sin(progress.toFloat() * (PI / 180).toFloat()) + circle.x + circle.width / 2 - pad.width / 2)
                pad.y =
                    (circle.height.toFloat() / 2 * cos(progress.toFloat() * (PI / 180).toFloat()) + circle.y + circle.width / 2 - pad.height / 2)
                pad.rotation = 360 - progress.toFloat()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //
            }
        })
        updateBall.run()
    }
}