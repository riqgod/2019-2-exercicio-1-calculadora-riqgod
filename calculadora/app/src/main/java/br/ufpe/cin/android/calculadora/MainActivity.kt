package br.ufpe.cin.android.calculadora

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //guarda referencia para a entrada e para a tela
        var editText = findViewById<EditText>(R.id.text_calc)
        var textView = findViewById<TextView>(R.id.text_info)

        //recebe o q tava guardado qdo mudou configuração
        var savedStateDigitado = savedInstanceState?.getString("digitado");
        var savedStateResultado = savedInstanceState?.getString("resultado");

        editText.setText(savedStateDigitado)
        textView.setText(savedStateResultado)

        print("listening..")
        setButtonListeners(editText,textView)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("digitado",findViewById<EditText>(R.id.text_calc).text.toString())
        outState.putString("resultado",findViewById<TextView>(R.id.text_info).text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun setButtonListeners(editText:EditText,textView:TextView){

        //cria os listeners
        //numeros
        findViewById<Button>(R.id.btn_0).setOnClickListener { editText.append("0") }
        findViewById<Button>(R.id.btn_1).setOnClickListener { editText.append("1") }
        findViewById<Button>(R.id.btn_2).setOnClickListener { editText.append("2") }
        findViewById<Button>(R.id.btn_3).setOnClickListener { editText.append("3") }
        findViewById<Button>(R.id.btn_4).setOnClickListener { editText.append("4") }
        findViewById<Button>(R.id.btn_5).setOnClickListener { editText.append("5") }
        findViewById<Button>(R.id.btn_6).setOnClickListener { editText.append("6") }
        findViewById<Button>(R.id.btn_7).setOnClickListener { editText.append("7") }
        findViewById<Button>(R.id.btn_8).setOnClickListener { editText.append("8") }
        findViewById<Button>(R.id.btn_9).setOnClickListener { editText.append("9") }

        //funcoes matematicas listeners
        findViewById<Button>(R.id.btn_Subtract).setOnClickListener { editText.setText(editText.text.toString()+"-")}
        findViewById<Button>(R.id.btn_Add).setOnClickListener { editText.setText(editText.text.toString()+"+") }
        findViewById<Button>(R.id.btn_Divide).setOnClickListener { editText.setText(editText.text.toString()+"/") }
        findViewById<Button>(R.id.btn_Multiply).setOnClickListener { editText.setText(editText.text.toString()+"*") }
        findViewById<Button>(R.id.btn_Power).setOnClickListener { editText.setText(editText.text.toString()+"^") }

        //utilities listeners
        findViewById<Button>(R.id.btn_LParen).setOnClickListener { editText.setText(editText.text.toString()+"(") }
        findViewById<Button>(R.id.btn_RParen).setOnClickListener { editText.setText(editText.text.toString()+")") }
        findViewById<Button>(R.id.btn_Dot).setOnClickListener { editText.append(".") }

        //clear button listener
        findViewById<Button>(R.id.btn_Clear).setOnClickListener{ editText.setText("")}

        //equal listener

        findViewById<Button>(R.id.btn_Equal).setOnClickListener{
            try {
                //tenta ver se o eval não dá erro
                val resultado = eval(editText.text.toString())
                textView.setText(editText.text.toString()+" = "+resultado);
                editText.setText("");

            }catch (err: RuntimeException){
                //se der erro, dispara mensagem
                Toast.makeText(applicationContext,"Error! "+err.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
        }


    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
