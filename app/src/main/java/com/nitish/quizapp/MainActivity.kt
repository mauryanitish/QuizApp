package com.nitish.quizapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitish.quizapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private var start = 0
    lateinit var binding: ActivityMainBinding
    var indexStore:Int = 0
    var count:Int = 0
    var randomData = listOf<QuestionReader>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val qusLine = readJsonFromAssets(this,"question.json")
        if (qusLine.isNotEmpty()) {
            val quizType = object : TypeToken<List<QuestionReader>>() {}.type
            val data:List<QuestionReader> = Gson().fromJson(qusLine,quizType)
            Log.d("data", data.toString())
            randomData = data.shuffled().take(10)
            displayQustions(indexStore)
        }

        binding.btnSubmit.setOnClickListener {
            if (start == 0){
                start =1
                timerStart()
                
            }
            if (binding.radioGroup.checkedRadioButtonId != -1) {

                val selectedOption = when (binding.radioGroup.checkedRadioButtonId) {
                    binding.op1.id -> "A"
                    binding.op2.id -> "B"
                    binding.op3.id -> "C"
                    binding.op4.id -> "D"
                    else -> null
                }

                val correctAnswer = randomData[indexStore - 1].answer

                if (selectedOption == correctAnswer) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                    count++
                } else {
                    Toast.makeText(
                        this,
                        "Wrong. The correct answer is $correctAnswer",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.radioGroup.clearCheck()

                if (indexStore == 10) {
                   dialog()
                } else
                    if (indexStore <= 10) {
                        displayQustions(indexStore)
                    }
            }else{
                Toast.makeText(this,"Please select any option.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    public fun dialog(){
        AlertDialog.Builder(this)
            .setTitle("Your total number of correct answer : ")
            .setMessage(count.toString() + " outs of 10")
            .setNegativeButton("Retry") { dialog, _ ->
                dialog.dismiss()
                Intent(this, MainActivity::class.java).let {
                    finish()
                    startActivity(it)
                }
            }
            .setPositiveButton("Exit") { dialog, _ ->
                dialog.dismiss()
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }
    private fun displayQustions(index:Int) {
        val qus = randomData[index]
        indexStore = index+1
        binding.tvNo.text = "Q "+indexStore
        binding.tvQuestion.text = qus.question

            binding.op1.text = qus.A
            binding.op2.text = qus.B
            binding.op3.text = qus.C
            binding.op4.text = qus.D

    }

    private fun readJsonFromAssets(context: Context, path: String) :String{
        val identifiers = "[ReadJSON]"
        try {
            val file = context.assets.open(path)

            val bufferReader = BufferedReader(InputStreamReader(file))
            val stringBuilder = StringBuilder()

            bufferReader.useLines { lines ->
                lines.forEach {
                    stringBuilder.append(it)
                }
            }
            val jsonString = stringBuilder.toString()

            return jsonString
        }
        catch (e:Exception){
            e.printStackTrace()
            return ""
        }
    }

   public fun timerStart(){
       var countDownTimer = object : CountDownTimer(60000, 1000) {
           override fun onTick(millisUntilFinished: Long) {
               binding.timer.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
           }
           override fun onFinish() {
               indexStore==10
               Toast.makeText(this@MainActivity,"Time Out !",Toast.LENGTH_SHORT).show()
              dialog()
           }
       }.start()
   }
}