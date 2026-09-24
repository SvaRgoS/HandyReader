#include <cassert>
#include <string>

#include "encoding_util.h"

int main() {
    const std::string cp1251Hello = "\xCF\xF0\xE8\xE2\xE5\xF2";

    assert(encoding_util::detect_xml_encoding(
        "<?xml version=\"1.0\" encoding=\"windows-1251\"?>") == "windows-1251");
    assert(encoding_util::detect_xml_encoding(
        "\xEF\xBB\xBF<?xml version=\"1.0\" encoding=\"koi8-r\"?>") ==
        encoding_util::ENC_KOI8R);
    assert(encoding_util::detect_xml_encoding(
        "<!-- encoding=\"windows-1251\" -->\n<fiction-book>Привет</fiction-book>") ==
        encoding_util::ENC_UTF8);
    assert(encoding_util::to_utf8(cp1251Hello, encoding_util::ENC_WIN1251) == u8"Привет");
    assert(encoding_util::to_utf8("\x89\x99\x9B", encoding_util::ENC_WIN1251) == u8"‰™›");
    return 0;
}
