package ownradio.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by a.polunina on 11.01.2017.
 * Класс утилита для декодирования строк
 */
public class DecodeUtil {

	//Функция декодирует "Cp1252" в "Cp1251"
	public static String Decode(String str) {
		if(str == null)
			return str;
		try {
			if (str.chars().anyMatch(c -> c >= 'А' - 848 && c <= 'ё' - 848)) {
				return new String(str.getBytes("Cp1252"), "Cp1251");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return str;
	}
}
