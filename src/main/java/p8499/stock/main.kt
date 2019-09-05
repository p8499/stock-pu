package p8499.stock

import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import java.io.File
import java.net.URL


fun main(args: Array<String>) {
    val map = build()
    File("""C:\sc\p20u.txt""").apply {
        if (exists()) delete()
        createNewFile()
        find(map, 5).forEach { appendText("$it\n") }
    }

    File("""C:\sc\p60u.txt""").apply {
        if (exists()) delete()
        createNewFile()
        find(map, 4).forEach { appendText("$it\n") }
    }
    File("""C:\sc\p120u.txt""").apply {
        if (exists()) delete()
        createNewFile()
        find(map, 3).forEach { appendText("$it\n") }
    }
    File("""C:\sc\p250u.txt""").apply {
        if (exists()) delete()
        createNewFile()
        find(map, 2).forEach { appendText("$it\n") }
    }
}

fun find(map: Map<String, Rating>, target: Int): List<String> {
    val symbolList = ArrayList<String>()
    map.forEach { symbol, rating -> if (rating.conclusion == target) symbolList.add(symbol) }
    return symbolList
}

fun build(): Map<String, Rating> {
    val map = HashMap<String, Rating>()
    val cleaner = HtmlCleaner()
    var page = 1
    while (page > 0) {
        val url = "http://www.chaguwang.cn/data/pingji/index3.php?last=30&num=200&p=$page"
        val html = cleaner.clean(URL(url))
        val trList = html.evaluateXPath("/body[1]/div[2]/div[2]/table[1]/tbody[1]/tr[position()>1]").map { it as TagNode }
        for (tr in trList) {
            val symbol = tr.evaluateXPath("/td[1]/a[1]/text()")[0].toString().let {
                when {
                    it[0] in arrayOf('6') -> "SHSE.$it"
                    it[0] in arrayOf('0', '3') -> "SZSE.$it"
                    else -> it
                }
            }
            val buy = tr.evaluateXPath("/td[5]/text()")[0].toString().toInt()
            val overweight = tr.evaluateXPath("/td[6]/text()")[0].toString().toInt()
            val neutral = tr.evaluateXPath("/td[7]/text()")[0].toString().toInt()
            val underweight = tr.evaluateXPath("/td[8]/text()")[0].toString().toInt()
            val sell = tr.evaluateXPath("/td[9]/text()")[0].toString().toInt()
            if (map.contains(symbol)) map[symbol]?.let {
                it.buy += buy
                it.overweight += overweight
                it.neutral += neutral
                it.underweight += underweight
                it.sell += sell
            }
            else map.put(symbol, Rating(buy, overweight, neutral, underweight, sell))
        }
        println(url)
        page = if (trList.isNotEmpty()) page + 1 else 0
    }
    return map
}

class Rating(var buy: Int = 0, var overweight: Int = 0, var neutral: Int = 0, var underweight: Int = 0, var sell: Int = 0) {
    val conclusion: Int
        get() = when {
            sell > 0 -> 1
            underweight > 0 -> 2
            neutral > 0 -> 3
            overweight > 0 -> 4
            buy > 0 -> 5
            else -> 0
        }
}