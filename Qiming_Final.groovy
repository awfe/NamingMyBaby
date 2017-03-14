@Grapes(
		@Grab(group = 'com.github.stuxuhai', module = 'jpinyin', version = '1.1.8')
)


import com.github.stuxuhai.jpinyin.*

class Constants {

	final static OUTPUT_FILE_NAME = 'output.txt'
	final static CHAR_2_FILE_NAME = '2.txt'		//第二个字
	final static CHAR_3_FILE_NAME = '3.txt'		//第三个字
	final static EXCEPT_FILE_NAME = 'except.txt'//要排除的字放在except.txt，当然你也可以直接在2.txt和3.txt删除
	final static FAMILY_NAME = '胡'				//姓

	final static FAMILY_NAME_IN_PINYIN = PinyinHelper.convertToPinyinArray(Constants.FAMILY_NAME as char, PinyinFormat.WITH_TONE_NUMBER)

}

pinyincache = [:]

def exceptFile = new File(Constants.EXCEPT_FILE_NAME)

def except = ''

if (exceptFile.exists()) {
	except = exceptFile.text
}

def char2 = new File(Constants.CHAR_2_FILE_NAME).text.findAll { !except.contains(it) }
def char3 = new File(Constants.CHAR_3_FILE_NAME).text.findAll { !except.contains(it) }

println char2
println char3

def ordered = []
char3.each { c ->
	char2.each {
		if (it != c) {
			ordered << checkByPinYin(Constants.FAMILY_NAME + it + c)
		}
	}
}

map = ordered.groupBy { it.pinyin }
def outPut = new File(Constants.OUTPUT_FILE_NAME)
outPut.delete();
map = map.sort { a, b ->
	def a1 = a.value.min { it.value }
	def b1 = b.value.min { it.value }
	return a1.value <=> b1.value
}
def i = 1;
map.each {
	def val = ''
	it.value.sort({ v -> v.value }).each { a -> val = val + '\t' + a.characters + '\t' + a.pinyin.padRight(12) + '\t(' + a.value + '. ' + a.message + ')\r\n' }
	outPut << i++ + '\t' + it.key + '\r\n' + val + '\r\n'
}

def checkByPinYin(s) {

	def char1 = convert(Constants.FAMILY_NAME_IN_PINYIN)//姓的拼音
	def char2PinyinList = getPinYinList(s.charAt(1), pinyincache)
	def char3PinyinList = getPinYinList(s.charAt(2), pinyincache)

	//以下为胡姓谐音避讳专用
	//忌讳的发音，可以看这里 https://www.zhihu.com/question/27715665/answer/38170553
	def avoidCharcters = [
		'li', //狐狸
		'shuo', //胡说
		'luo', //胡萝卜
		'zi', //胡子
		'tu', //糊涂
		'jing', //狐精
		'peng', //狐朋狗友
		'ou', //狐臭
		'yan' //胡言乱语

	]

	for (String a : avoidCharcters) {
		Result result = avoid(a, char2PinyinList, char3PinyinList, s)

		if (result) {
			return result
		}
	}

	//叠字已经在组合的时候处理掉了

	//以下为通用规则

	def result = check(180, '第二三个字声母与姓相同', s) {
		def py2 = char2PinyinList.find { it.consonant == char1.consonant }
		def py3 = char3PinyinList.find { it.consonant == char1.consonant }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}
	if (result) {
		return result
	}

	result = check(170, '第二三个字韵母与姓相同', s) {
		def py2 = char2PinyinList.find { it.vowel == char1.vowel }
		def py3 = char3PinyinList.find { it.vowel == char1.vowel }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}
	if (result) {
		return result
	}

	result = check(160, '第二三个字的韵母\'uan\'\'uang\'', s) {
		def py2 = char2PinyinList.find { it.vowel == 'uan' || it.vowel == 'uang' }
		def py3 = char3PinyinList.find { it.vowel == 'uan' || it.vowel == 'uang' }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}
	if (result) {
		return result
	}

	result = check(150, '二三个字的韵母\'un\' \'ue\',\'v\'或者是\'yu\'', s) {
		def py2 = char2PinyinList.find { it.vowel == 'uan' || it.vowel == 'uang' }
		def py3 = char3PinyinList.find { it.vowel == 'uan' || it.vowel == 'uang' }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}
	if (result) {
		return result
	}

	result = check(140, '第二三个字的韵母\'ong\'', s) {
		def py2 = char2PinyinList.find { it.vowel == 'ong' || it.vowel == 'iong' }
		def py3 = char3PinyinList.find { it.vowel == 'ong' || it.vowel == 'iong' }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(130, '第二三个字的韵母\'eng\'', s) {
		def py2 = char2PinyinList.find { it.vowel == 'eng' }
		def py3 = char3PinyinList.find { it.vowel == 'eng' }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(120, '第二三个字的韵母\'ei\'或者\'ui\'', s) {
		def py2 = char2PinyinList.find { it.vowel == 'ui' || it.vowel == 'ei' }
		def py3 = char3PinyinList.find { it.vowel == 'ui' || it.vowel == 'ei' }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(110, '第二三个字韵母相同', s) {
		def py3
		def py2 = char2PinyinList.find { c2 -> char3PinyinList.find({ c3 -> py3 = c3; return c2.vowel == c3.vowel }) }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(100, '第二三个字声母相同', s) {
		def py3
		def py2 = char2PinyinList.find { c2 -> char3PinyinList.find({ c3 -> py3 = c3; return c2.consonant == c3.consonant }) }

		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(90, '第二三个字都是三声', s) {

		def py2 = char2PinyinList.find { it.tone == '3' }
		def py3 = char3PinyinList.find { it.tone == '3' }
		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}


	result = check(80, '第二三个字都是二声', s) {

		def py2 = char2PinyinList.find { it.tone == '2' }
		def py3 = char3PinyinList.find { it.tone == '2' }
		if (py2 && py3) {
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}


	result = check(70, '第二个字或第三个字不响亮', s) {

		def v = ['uan', 'uang', 'un', 'ong', 'iong', 'eng', 'ou', 'o']

		def py2 = char2PinyinList.find { v.contains(it.vowel) }
		def py3 = char3PinyinList.find { v.contains(it.vowel) }

		if (py2 || py3) {

			py2 = py2 ?: char2PinyinList[0]
			py3 = py3 ?: char3PinyinList[0]

			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}


	result = check(60, '第二个字的韵母与姓相同', s) {

		def py2 = char2PinyinList.find { it.consonant == char1.consonant }

		if (py2) {
			def py3 = char3PinyinList[0]
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}



	result = check(50, '第三个字的韵母与姓相同', s) {

		def py3 = char3PinyinList.find { it.consonant == char1.consonant }

		if (py3) {
			def py2 = char2PinyinList[0]
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(40, '第二个字是三声', s) {

		def py2 = char2PinyinList.find { it.tone == '3' }

		if (py2) {
			def py3 = char3PinyinList[0]
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}


	result = check(30, '声母和英语发音不一致的，以防得了诺贝尔奖老外念不出名字', s) {


		def v = ['x', 'q', 'j', 'z', 'zh']

		def py2 = char2PinyinList.find { v.contains(it.consonant) }
		def py3 = char3PinyinList.find { v.contains(it.consonant) }

		if (py2 || py3) {

			py2 = py2 ?: char2PinyinList[0]
			py3 = py3 ?: char3PinyinList[0]

			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(20, '第二个字是多音字', s) {

		def b = char2PinyinList.size() > 1

		if (b) {
			def py2 = char2PinyinList[0]
			def py3 = char3PinyinList[0]
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	result = check(10, '第三个字是多音字', s) {

		def b = char3PinyinList.size() > 1

		if (b) {
			def py2 = char2PinyinList[0]
			def py3 = char3PinyinList[0]
			return py2.consonant + py2.vowel + py3.consonant + py3.vowel
		} else {
			return null
		}
	}

	if (result) {
		return result
	}

	def py2 = char2PinyinList[0]
	def py3 = char3PinyinList[0]

	return new Result(value: 0, message: '正常', characters: s, pinyin: py2.consonant + py2.vowel + py3.consonant + py3.vowel);
}

private Result avoid(toCheck, char2PinyinList, char3PinyinList, s) {
	def found = char2PinyinList.find { it.pinyin_without_tone.endsWith(toCheck) }
	if (found) {
		return new Result(value: 200, message: "第二字发音为\'$toCheck\'", characters: s, pinyin: found.consonant + found.vowel + char3PinyinList[0].consonant + char3PinyinList[0].vowel)
	}
	found = char3PinyinList.find { it.pinyin_without_tone.endsWith(toCheck) }
	if (found) {
		return new Result(value: 200, message: "第三字发音为\'$toCheck\'", characters: s, pinyin: char2PinyinList[0].consonant + char2PinyinList[0].vowel + found.consonant + found.vowel)
	}
	return null

}

def check(value, msg, s, Closure closure) {
	def char23 = closure.call()
	if (char23) {
		return new Result(value: value, message: msg, characters: s, pinyin: char23)
	}
}

def getPinYinList(c, pinyincache) {
	def pinyinList
	if (pinyincache[c] == null) {
		pinyinList = PinyinHelper.convertToPinyinArray(c, PinyinFormat.WITH_TONE_NUMBER)
		pinyincache[c] = convert(pinyinList)
	}
	pinyinList = pinyincache[c]
	return pinyinList
}

def convert(pinyinArray) {
	def out = []
	for (pinyin in pinyinArray) {
		out << splitPinYin(pinyin)
	}
	return out;
}

/*
把拼音的声母，韵母和声调分开
*/

def PinYin splitPinYin(pinyin) {
	def out = []

	if (pinyin != null) {

		def yunmu = ['a', 'e', 'i', 'o', 'u', 'v']
		def position1 = pinyin.length()
		def position2 = pinyin.length() - 1
		yunmu.each {
			def p = pinyin.indexOf(it)
			if (p != -1 && position1 > p) {
				position1 = p;
			}
		}

		out[0] = pinyin.substring(0, position1)
		out[1] = pinyin.substring(position1, position2)
		out[2] = pinyin.substring(position2)
	}
	return new PinYin(pinyin: pinyin, consonant: out[0], vowel: out[1], tone: out[2], pinyin_without_tone: out[0] + out[1])
}

def class PinYin {
	String pinyin
	String pinyin_without_tone
	String consonant
	String vowel
	String tone
}

def class Result {
	int value
	String pinyin
	String characters
	String message

}
