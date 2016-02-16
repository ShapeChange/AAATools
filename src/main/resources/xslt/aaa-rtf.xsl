<?xml version="1.0" encoding="Windows-1252"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- 

     Version 1.0 - 12.05.2001
	  Version 1.1 - 26.05.2001 (TOC-Definition angepasst)
	  Version 1.2 - 28.07.2001
	  Version 1.5 - 03.01.2001
	  Version 1.7 - 30.03.2003
	  Version 1.10 - 29.07.2005
	  Version 1.11 - 12.08.2008
  	Version 1.12 - 24.10.2011
  	Version 1.13 - 23.01.2013

     (c) 2001-2013 interactive instruments GmbH, Bonn
     im Auftrag der AdV, Arbeitsgemeinschaft der Vermessungsverwaltungen der
     Länder der Bundesrepublik Deutschland

	  http://www.adv-online.de/

  -->
<xsl:output method="text" encoding="Windows-1252"/>
<xsl:decimal-format name="code" NaN="999999"/>
<xsl:template match="/">{\rtf1\ansi\ansicpg1252\uc1 \deff0\deflang1031\deflangfe1031{\fonttbl{\f0\froman\fcharset0\fprq2{\*\panose 02020603050405020304}Times New Roman;}{\f3\froman\fcharset2\fprq2{\*\panose 05050102010706020507}Symbol;}
{\f8\froman\fcharset0\fprq2{\*\panose 00000000000000000000}Tms Rmn;}{\f102\froman\fcharset238\fprq2 Times New Roman CE;}{\f103\froman\fcharset204\fprq2 Times New Roman Cyr;}{\f105\froman\fcharset161\fprq2 Times New Roman Greek;}
{\f106\froman\fcharset162\fprq2 Times New Roman Tur;}{\f107\froman\fcharset177\fprq2 Times New Roman (Hebrew);}{\f108\froman\fcharset178\fprq2 Times New Roman (Arabic);}{\f109\froman\fcharset186\fprq2 Times New Roman Baltic;}}
{\colortbl;\red0\green0\blue0;\red0\green0\blue255;\red0\green255\blue255;\red0\green255\blue0;\red255\green0\blue255;\red255\green0\blue0;\red255\green255\blue0;\red255\green255\blue255;\red0\green0\blue128;\red0\green128\blue128;\red0\green128\blue0;
\red128\green0\blue128;\red128\green0\blue0;\red128\green128\blue0;\red128\green128\blue128;\red192\green192\blue192;\red255\green255\blue255;}{\stylesheet{\ql \li0\ri0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 
\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \snext0 Normal;}{\s1\ql \fi-425\li425\ri0\sl288\slmult1\keep\keepn\pagebb\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\adjustright\rin0\lin425\itap0 
\b\fs28\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext15 heading 1,\'fc1;}{\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl1\outlinelevel0\adjustright\rin0\lin567\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon1 \snext15 heading 2,\'fc2;}{\s3\ql \fi-851\li851\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl2\outlinelevel1\adjustright\rin0\lin851\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon2 \snext15 heading 3,\'fc3;}{\s4\ql \fi-992\li992\ri0\sb360\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl3\outlinelevel2\adjustright\rin0\lin992\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon3 \snext15 heading 4;}{\s5\ql \fi-425\li425\ri0\sb300\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl4\outlinelevel3\adjustright\rin0\lin425\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon4 \snext15 heading 5;}{\s6\ql \fi-425\li425\ri0\sb240\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl5\outlinelevel4\adjustright\rin0\lin425\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon5 \snext15 heading 6;}{\s7\ql \fi-425\li425\ri0\sb240\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl6\outlinelevel5\adjustright\rin0\lin425\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon6 \snext15 heading 7;}{\s8\ql \fi-425\li425\ri0\sb240\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl7\outlinelevel6\adjustright\rin0\lin425\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon7 \snext15 heading 8;}{\s9\ql \fi-425\li425\ri0\sb240\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\aspalpha\aspnum\faauto\ls1\ilvl8\outlinelevel7\adjustright\rin0\lin425\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon8 \snext15 heading 9;}{\*\cs10 \additive Default Paragraph Font;}{\s15\qj \li0\ri0\sb240\sl360\slmult0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 
\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext15 ab;}{\s16\ql \li0\ri85\sb240\widctlpar\brdrt\brdrs\brdrw15\brsp60 \brdrl\brdrs\brdrw15\brsp60 \brdrb\brdrs\brdrw15\brsp60 \brdrr\brdrs\brdrw15\brsp60 
\tqc\tx4536\tqr\tx9072\aspalpha\aspnum\faauto\adjustright\rin85\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext16 header;}{\s17\ql \fi-283\li284\ri4\sb240\sa120\sl-320\slmult0\keep\widctlpar
\tqr\tldot\tx8756\tqr\tldot\tx8788\aspalpha\aspnum\faauto\adjustright\rin4\lin284\itap0 \b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext0 \sautoupd toc 1;}{\s18\ql \fi-425\li709\ri4\sa120\sl-320\slmult0\keep\widctlpar
\tqr\tldot\tx8756\tqr\tldot\tx8788\aspalpha\aspnum\faauto\adjustright\rin4\lin709\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon17 \snext0 \sautoupd toc 2;}{\s19\ql \fi-567\li1276\ri4\sa120\sl-320\slmult0\keep\widctlpar
\tqr\tldot\tx8756\tqr\tldot\tx8788\aspalpha\aspnum\faauto\adjustright\rin4\lin1276\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon18 \snext0 \sautoupd toc 3;}{\s20\ql \fi-709\li1985\ri4\sa120\sl-320\slmult0\widctlpar
\tqr\tldot\tx8756\tqr\tldot\tx8788\aspalpha\aspnum\faauto\adjustright\rin4\lin1985\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon19 \snext0 \sautoupd toc 4;}{\s21\qj \li0\ri0\sb240\sl360\slmult0
\keep\keepn\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext15 ab\'fc;}{\s22\qc \fi-1247\li1247\ri0\sb360\sl360\slmult0\keepn\widctlpar
\tx1247\aspalpha\aspnum\faauto\adjustright\rin0\lin1247\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext23 bi;}{\s23\qj \fi-1247\li1247\ri0\sb240\sl-360\slmult0\keep\widctlpar
\tx1247\aspalpha\aspnum\faauto\adjustright\rin0\lin1247\itap0 \i\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext15 bu;}{\s24\qj \fi-425\li425\ri0\sb120\sl360\slmult0\widctlpar\tx425{\*\pn \pnlvlbody\ilvl11\ls2047\pnrnot0
\pndec\pnb0\pni0\pnstart1\pnindent283\pnhang{\pntxtb (}{\pntxta )}}\aspalpha\aspnum\faauto\ls2047\ilvl11\adjustright\rin0\lin425\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon25 \snext24 en1;}{
\s25\qj \fi-425\li425\ri0\sb120\sl360\slmult0\widctlpar\tx425{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb \'b7}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin425\itap0 
\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext25 ei1;}{\s26\qj \li561\ri567\sb120\sl360\slmult0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin567\lin561\itap0 \i\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\sbasedon15 \snext15 def;}{\s27\qj \fi-425\li850\ri0\sb80\sl360\slmult0\widctlpar\tx425\tx851{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin850\itap0 
\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon25 \snext27 ei2;}{\s28\qj \fi-425\li1276\ri0\sb80\sl360\slmult0\widctlpar\tx1276{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}
\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin1276\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon27 \snext28 ei3;}{\s29\ql \fi-1418\li1418\ri0\sb240\sl360\slmult0
\keep\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin1418\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext29 lit;}{\s30\qj \li0\ri0\sb240\sl360\slmult0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 
\i\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext30 abk;}{\s31\qj \li425\ri0\sb120\sl360\slmult0\widctlpar\tx425{\*\pn \pnlvlbody\ilvl0\ls2047\pnrnot0\pndec\pnf8 }\aspalpha\aspnum\faauto\ls2047\adjustright\rin0\lin425\itap0 
\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon25 \snext31 e1;}{\s32\qj \fi-425\li425\ri0\sb120\sl360\slmult0\keepn\widctlpar\tx425{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb \'b7}}
\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin425\itap0 \b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon25 \snext25 ei1f;}{\s33\qj \fi-425\li850\ri0\sb80\sl360\slmult0\widctlpar\tx425\tx851{\*\pn 
\pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin850\itap0 \b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon27 \snext27 ei2f;}{
\s34\qj \fi-425\li1276\ri0\sb80\sl360\slmult0\widctlpar\tx1276{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin1276\itap0 
\b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon28 \snext28 ei3f;}{\s35\qj \fi-427\li851\ri0\sb80\sl360\slmult0\widctlpar\tx425\tx851{\*\pn \pnlvlbody\ilvl11\ls2047\pnrnot0\pndec\pnstart1\pnindent283\pnhang{\pntxta .}}
\aspalpha\aspnum\faauto\ls2047\ilvl11\adjustright\rin0\lin851\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon27 \snext35 en2;}{\s36\qj \fi-560\li560\ri0\sb240\widctlpar\tx560\aspalpha\aspnum\faauto\adjustright\rin0\lin560\itap0 
\fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext36 footnote text;}{\*\cs37 \additive \fs18\up6 \sbasedon10 footnote reference;}{\s38\qr \li0\ri85\sb240\widctlpar\brdrt\brdrs\brdrw15\brsp40 \brdrl\brdrs\brdrw15\brsp40 \brdrb
\brdrs\brdrw15\brsp40 \brdrr\brdrs\brdrw15\brsp40 \aspalpha\aspnum\faauto\adjustright\rin85\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext38 footer;}{\*\cs39 \additive \sbasedon10 page number;}{
\s40\qc \li0\ri0\sb480\sa480\sl480\slmult0\keep\keepn\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \b\fs36\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext0 t;}{
\s41\ql \li0\ri0\sb40\sa60\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext41 tab;}{
\s42\qc \li0\ri0\sb40\sa60\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon41 \snext42 tabz;}{\s43\qj \li454\ri0\sb120\sl300\slmult0
\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin454\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext15 Anmerkung,an;}{\s44\qc \li0\ri0\sa800\sl480\slmult0
\keep\keepn\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \i\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon40 \snext15 ut;}{\s45\ql \li0\ri0\sb60\sa60\widctlpar
\tx1418\tx6237\tx7513\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon16 \snext45 okt;}{\*\cs46 \additive \strike \sbasedon10 Durchstreichen,d;}{
\s47\ql \li0\ri0\sb40\sa60\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \b\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon41 \snext41 tab\'fc;}{\s48\qj \li851\ri0\sb80\sl360\slmult0\widctlpar\tx1276{\*\pn 
\pnlvlbody\ilvl0\ls2047\pnrnot0\pndec\pnf8 }\aspalpha\aspnum\faauto\ls2047\adjustright\rin0\lin851\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon28 \snext48 e3;}{\s49\ql \li1418\ri0\sl360\slmult0\widctlpar
\tx1276\aspalpha\aspnum\faauto\adjustright\rin0\lin1418\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext49 e4;}{\s50\qj \fi-425\li850\ri0\sb80\sl360\slmult0\widctlpar\tx425\tx851{\*\pn \pnlvlbody\ilvl0\ls2047\pnrnot0
\pndec\pnf8 }\aspalpha\aspnum\faauto\ls2047\adjustright\rin0\lin850\itap0 \fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon27 \snext50 e2;}{\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 
\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon15 \snext51 okab;}{\s52\qj \fi-1418\li1418\ri0\sa60\sl240\slmult0\widctlpar\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin1418\itap0 
\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon51 \snext52 oke1;}{\s53\qj \fi-1418\li2836\ri0\sa60\sl240\slmult0\widctlpar\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin2836\itap0 
\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon52 \snext53 oke2;}{\s54\ql \li0\ri0\sb60\sa60\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext54 
ok\'fc;}{\s55\qj \li0\ri0\sl240\slmult0\widctlpar\tx1418\tx6167\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon52 \snext55 okw;}{
\s56\ql \li0\ri0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon0 \snext56 ok\'fcb;}{\s57\qj \fi-283\li1701\ri0\sa60\sl240\slmult0\widctlpar\tx1418{\*\pn 
\pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin1701\itap0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon53 \snext57 okei2;}{
\s58\qj \fi-1418\li4253\ri0\sa60\sl240\slmult0\widctlpar\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin4253\itap0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon53 \snext58 oke3;}{\s59\qj \fi-284\li1985\ri0\sa60\sl240\slmult0
\widctlpar\tx1418{\*\pn \pnlvlblt\ilvl10\ls2047\pnrnot0\pnf3\pnstart1\pnindent283\pnhang{\pntxtb -}}\aspalpha\aspnum\faauto\ls2047\ilvl10\adjustright\rin0\lin1985\itap0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 \sbasedon57 \snext59 okei3;}}
{\*\listtable{\list\listtemplateid-572103286{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'01\'00;}{\levelnumbers\'01;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s1
}{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'03\'00.\'01;}{\levelnumbers\'01\'03;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s2}{\listlevel\levelnfc0\levelnfcn0
\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'05\'00.\'01.\'02;}{\levelnumbers\'01\'03\'05;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s3}{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0
\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'07\'00.\'01.\'02.\'03;}{\levelnumbers\'01\'03\'05\'07;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s4}{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0
\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'09\'00.\'01.\'02.\'03.\'04;}{\levelnumbers\'01\'03\'05\'07\'09;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s5}{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0
\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'0b\'00.\'01.\'02.\'03.\'04.\'05;}{\levelnumbers\'01\'03\'05\'07\'09\'0b;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s6}{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0
\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'0d\'00.\'01.\'02.\'03.\'04.\'05.\'06;}{\levelnumbers\'01\'03\'05\'07\'09\'0b\'0d;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s7}{\listlevel\levelnfc0\levelnfcn0
\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'0f\'00.\'01.\'02.\'03.\'04.\'05.\'06.\'07;}{\levelnumbers\'01\'03\'05\'07\'09\'0b\'0d\'0f;}\chbrdr\brdrnone\brdrcf1 \chshdng0\chcfpat1\chcbpat1 \s8}{\listlevel
\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelold\levelspace144\levelindent0{\leveltext\'11\'00.\'01.\'02.\'03.\'04.\'05.\'06.\'07.\'08;}{\levelnumbers\'01\'03\'05\'07\'09\'0b\'0d\'0f\'11;}\chbrdr\brdrnone\brdrcf1 
\chshdng0\chcfpat1\chcbpat1 \s9}{\listname ;}\listid-5}}{\*\listoverridetable{\listoverride\listid-5\listoverridecount0\ls1}}{\info{\title Objektartenkatalog}{\author NN}{\operator Clemens Portele}{\creatim\yr2001\mo3\dy24\hr19\min25}
{\revtim\yr2001\mo3\dy24\hr20\min3}{\printim\yr1997\mo11\dy18\hr9\min45}{\version3}{\edmins0}{\nofpages1}{\nofwords77}{\nofchars442}{\*\company interactive instruments}{\nofcharsws0}{\vern8269}}
\paperw11900\paperh16840\margl1417\margr1417\margt1417\margb1134 \deftab709\widowctrl\ftnbj\aenddoc\hyphhotz425\noxlattoyen\expshrtn\noultrlspc\dntblnsbdb\nospaceforul\linkstyles\hyphcaps0\hyphauto1\formshade\horzdoc\dghspace120\dgvspace120\dghorigin1701
\dgvorigin1984\dghshow1\dgvshow0\jexpand\viewkind1\viewscale100\pgbrdrhead\pgbrdrfoot\bdrrlswsix\nolnhtadjtbl\oldas 

{\field\fldedit{\*\fldinst { TOC \\o "1-3" \\t "\'dcberschrift 1.\'fc1;\'dcberschrift 2.\'fc2;1" \\h \\z }}}
{(An dieser Stelle steht das Inhaltsverzeichnis als Word-Feld. Um es sichbar zu machen, auf den Anfang des Dokuments positionieren, mit F9 den Inhalt aktualisieren und anschliessend den Text in dieser Klammer beseitigen.)}\par

\fet0\sectd \linex0\endnhere\sectdefaultcl 

\pard\plain 
\s1\ql \fi-425\li425\ri0\sl288\slmult1\keep\keepn\pagebb\widctlpar\hyphpar0\nooverflow\faroman\ls1\outlinelevel0\rin0\lin425\itap0 
\b\f4\fs28\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Objektartenkatalog: <xsl:value-of select="FC_FeatureCatalogue/name"/>
\par }

\pard\plain 
\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Versionsnummer 
\par }

\pard\plain 
\ql \li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0 \f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="FC_FeatureCatalogue/versionNumber"/>
\par {\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}}

\pard\plain 
\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Stand
\par }

\pard\plain 
\ql \li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0 \f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="FC_FeatureCatalogue/versionDate"/>
\par {\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}}

\pard\plain 
\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Anwendungsgebiet 
\par }

\pard\plain 
\ql \li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0 \f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="FC_FeatureCatalogue/scope"/>
\par {\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}}

\pard\plain 
\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Verantwortliche Institution 
\par }

\pard\plain 
\ql \li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0 \f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="FC_FeatureCatalogue/producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName"/>
\par {\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab} \par}

<xsl:variable name="nft" select="count(FC_FeatureCatalogue/producer/CI_ResponsibleParty/responsibility)" />
<xsl:choose><xsl:when test="$nft >= 1">

\pard\plain 
\ql \li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0 \f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Verantwortlichkeiten (siehe ISO 19115): \par}

<xsl:for-each select="FC_FeatureCatalogue/producer/CI_ResponsibleParty/responsibility"> 
{- <xsl:value-of select="."/> \par}
</xsl:for-each>

</xsl:when></xsl:choose>

<xsl:variable name="versionDate" select="FC_FeatureCatalogue/versionDate" />

\par

\par\sect
\fet0\sectd \linex0\endnhere\sectdefaultcl 
{\header \pard\plain \s16\ql \li0\ri85\sb240\widctlpar\brdrt\brdrs\brdrw15\brsp60 \brdrl
\brdrs\brdrw15\brsp60 \brdrb\brdrs\brdrw15\brsp60 \brdrr\brdrs\brdrw15\brsp60 \tqc\tx4536\tqr\tx8931\aspalpha\aspnum\faauto\adjustright\rin85\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Objektarten\'fcbersicht\tab \tab 
Stand: <xsl:value-of select="$versionDate"/>
\par \pard\plain\par }}
{\footer \pard\plain \s38\ql \li0\ri85\sb240\widctlpar\tqr\tx8931\brdrt\brdrs\brdrw15\brsp40 \brdrl\brdrs\brdrw15\brsp40 \brdrb\brdrs\brdrw15\brsp40 \brdrr\brdrs\brdrw15\brsp40 \aspalpha\aspnum\faauto\adjustleft\rin85\lin0\itap0 
\fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Abschnitt 7.1 der GeoInfoDok}\tab {Seite }{\field{\*\fldinst {\cs39  PAGE }}{\fldrslt {\cs39\lang1024\langfe1024\noproof 1}}}{
\par }}
\pard\plain 
\s1\ql \fi-425\li425\ri0\sl288\slmult1\keep\keepn\pagebb\widctlpar\hyphpar0\nooverflow\faroman\ls1\outlinelevel0\rin0\lin425\itap0 
\b\f4\fs28\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Objektarten\'fcbersicht
\par }

<xsl:for-each select="FC_FeatureCatalogue/AC_Objektartengruppe|FC_FeatureCatalogue/AC_Objektbereich">  
<xsl:sort select="format-number(./code,'000000','code')"/>
<xsl:variable name="objektart" select="." />
<xsl:variable name="nftx" select="count(/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id])" />
<xsl:if test="$nftx >= 0">
\pard\plain\ql\li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {\par\b <xsl:value-of select="$objektart/name"/>\b0\par}
<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType">
<xsl:sort select="format-number(./code,'000000','code')"/>
<xsl:variable name="featuretype" select="." />
<xsl:for-each select="Objektartengruppenzugehoerigkeit">
<xsl:choose>
<xsl:when test="@idref = $objektart/@id">
\tab <xsl:value-of select="$featuretype/name"/>\par
</xsl:when>
</xsl:choose>
</xsl:for-each>
</xsl:for-each>
</xsl:if>
</xsl:for-each>

<xsl:for-each select="FC_FeatureCatalogue/AC_Objektartengruppe|FC_FeatureCatalogue/AC_Objektbereich">  
<xsl:sort select="format-number(./code,'000000','code')"/>
<xsl:variable name="objektart" select="." />
<xsl:variable name="nftx" select="count(/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id])" />
<xsl:if test="$nftx >= 0">
\sect
\fet0\sectd \linex0\endnhere\sectdefaultcl 
{\header \pard\plain \s16\ql \li0\ri85\sb240\widctlpar\brdrt\brdrs\brdrw15\brsp60 \brdrl
\brdrs\brdrw15\brsp60 \brdrb\brdrs\brdrw15\brsp60 \brdrr\brdrs\brdrw15\brsp60 \tqc\tx4536\tqr\tx8931\aspalpha\aspnum\faauto\adjustright\rin85\lin0\itap0 \fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:choose><xsl:when test="count($objektart/Objektbereichzugehoerigkeit)=1"><xsl:text>Objektartengruppe: </xsl:text></xsl:when><xsl:otherwise><xsl:text>Objektartenbereich: </xsl:text></xsl:otherwise>	</xsl:choose><xsl:value-of select="name"/>\tab \tab 
Stand: <xsl:value-of select="$versionDate"/>
\par \pard\plain\par }}
{\footer \pard\plain \s38\ql \li0\ri85\sb240\widctlpar\tqr\tx8931\brdrt\brdrs\brdrw15\brsp40 \brdrl\brdrs\brdrw15\brsp40 \brdrb\brdrs\brdrw15\brsp40 \brdrr\brdrs\brdrw15\brsp40 \aspalpha\aspnum\faauto\adjustleft\rin85\lin0\itap0 
\fs20\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Version <xsl:value-of select="/FC_FeatureCatalogue/versionNumber"/>}\tab {Seite }{\field{\*\fldinst {\cs39  PAGE }}{\fldrslt {\cs39\lang1024\langfe1024\noproof 1}}}{
\par }}
\pard\plain 
\s1\ql \fi-425\li425\ri0\sl288\slmult1\keep\keepn\pagebb\widctlpar\hyphpar0\nooverflow\faroman\ls1\outlinelevel0\rin0\lin425\itap0 
\b\f4\fs28\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {{\*\bkmkstart _Toc<xsl:value-of select="id"/>}<xsl:value-of select="name"/>{\*\bkmkend _Toc<xsl:value-of select="id"/>}
\par }

<xsl:variable name="nft" select="count(definition)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
{\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}
\pard\plain \s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Bezeichnung, Definition \par }
<xsl:for-each select="definition">\pard\plain\ql\li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="."/>
\par} 
</xsl:for-each>
</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count(note)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
\pard\plain \s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {Anmerkungen, Konsistenzregeln \par }
<xsl:for-each select="note">\pard\plain\ql\li0\ri0\widctlpar\nooverflow\faroman\rin0\lin0\itap0\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="."/>
\par {\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}}
</xsl:for-each>
</xsl:when>
</xsl:choose>

\pard\plain
\par

<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType">
<xsl:sort select="format-number(./code,'000000','code')"/>
<xsl:variable name="featuretype" select="." />
<xsl:for-each select="Objektartengruppenzugehoerigkeit">
<xsl:choose>
<xsl:when test="@idref = $objektart/@id">

\pard\plain
\trowd 
\trgaph71\trleft-71\trhdr
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 
\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrs\brdrw15 
\clbrdrr\brdrs\brdrw15 
\clshdng1000\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain\pagebb 

\s2\ql \fi-567\li567\ri0\sb480\sl288\slmult1\keep\keepn\widctlpar\hyphpar0\nooverflow\faroman\ls1\ilvl1\outlinelevel1\rin0\lin567\itap0 
\b\f4\fs24\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 {<xsl:value-of select="$featuretype/name"/>\par }
{\listtext\pard\plain\s2 \b\f4 \hich\af4\dbch\af0\loch\f4 1.1\tab}

\pard\plain \s45\ql \li0\ri0\sb60\sa60\widctlpar\intbl\tx6237\tx7513\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{<xsl:variable name="nft" select="count($featuretype/bedeutung)"/>
<xsl:if test="$nft = 1"><xsl:value-of select="$featuretype/bedeutung"/>: </xsl:if>
<xsl:value-of select="$featuretype/name"/>\tab\tab{Kennung: <xsl:value-of select="$featuretype/code"/>}\cell }
\row 

<xsl:variable name="nft" select="count($featuretype/definition)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 
\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Definition:}{\b0 \cell }
\row

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 \clbrdrb\brdrs\brdrw15 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone \clbrdrb\brdrs\brdrw15
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{\cell <xsl:for-each select="$featuretype/definition"><xsl:value-of select="."/>\par </xsl:for-each>\cell }
\row

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/subtypeOf)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Abgeleitet aus:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/subtypeOf"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/wirdTypisiertDurch)" />
<xsl:choose>
<xsl:when test="$nft = 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Objekttyp:}
\b0\cell\row

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:value-of select="$featuretype/wirdTypisiertDurch"/>\par\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/modellart)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Modellart:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/modellart"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/grunddatenbestand)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Grunddatenbestand:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/grunddatenbestand"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/themen)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Themen:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/themen"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/Konsistenzbedingung)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Konsistenzbedingungen:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/Konsistenzbedingung"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/Bildungsregel)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Bildungsregeln:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/Bildungsregel"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/Erfassungskriterium)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Erfassungskriterien:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/Erfassungskriterium"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:variable name="nft" select="count($featuretype/Lebenszeitintervall)" />
<xsl:choose>
<xsl:when test="$nft >= 1">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Lebenszeitintervall:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
\cell <xsl:for-each select="$featuretype/Lebenszeitintervall"><xsl:value-of select="."/>\par </xsl:for-each>\cell\row 

</xsl:when>
</xsl:choose>

<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute">
<xsl:variable name="featureAtt" select="." />
<xsl:for-each select="$featuretype/characterizedBy">
<xsl:choose>
<xsl:when test=" @idref = $featureAtt/@id">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Attributart:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s52\qj \fi-1418\li1418\ri0\sa60\sl240\slmult0\widctlpar\intbl\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin1418 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031
\cell Bezeichnung:\tab <xsl:value-of select="$featureAtt/name"/>\cell\row 
<xsl:variable name="nft" select="count($featureAtt/code)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Kennung:\tab <xsl:value-of select="$featureAtt/code"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureAtt/ValueDataType)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Datentyp:\tab <xsl:value-of select="$featureAtt/ValueDataType"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureAtt/cardinality)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Kardinalit\'e4t:\tab <xsl:value-of select="$featureAtt/cardinality"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureAtt/modellart)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Modellart:\tab <xsl:value-of select="$featureAtt/modellart"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureAtt/grunddatenbestand)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Grunddatenb.:\tab <xsl:value-of select="$featureAtt/grunddatenbestand"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureAtt/definition)+count($featureAtt/objektbildend)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
\cell Definition:\tab <xsl:for-each select="$featureAtt/definition"><xsl:value-of select="."/>\par\tab </xsl:for-each><xsl:if test="$featureAtt/objektbildend=true">Diese Attributart ist objektbildend.\par\tab</xsl:if> \cell\row
</xsl:when>
</xsl:choose>
<xsl:choose>
<xsl:when test="$featureAtt/ValueDomainType = 1">
\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 
\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428
\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074
\pard\plain
\s52\qj \fi-1418\li1418\ri0\sa60\sl240\slmult0\widctlpar\intbl\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin1418 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031
\cell Wertearten:\par
\pard\plain
\s55\qj \li0\ri0\sl240\slmult0\widctlpar\intbl\tx1418\tx6167\nooverflow\faroman\rin0\lin0
{\tab Bezeichner\tab Wert\par}
<xsl:for-each select="/FC_FeatureCatalogue/FC_Value">
<xsl:variable name="fcvalue" select="." />
<xsl:for-each select="$featureAtt/enumeratedBy">
<xsl:choose>
<xsl:when test="$fcvalue/@id = @idref">
{\tab <xsl:value-of select="$fcvalue/label"/>\tab <xsl:value-of select="$fcvalue/code"/><xsl:if test="count($fcvalue/grunddatenbestand) >= 1"> (G)</xsl:if>\par}
<xsl:if test="count($fcvalue/definition)=1">{\li1600 \fs16 <xsl:value-of select="$fcvalue/definition"/> \par \fs20}</xsl:if>
</xsl:when>
</xsl:choose>
</xsl:for-each>
</xsl:for-each>
\cell\row
</xsl:when>
</xsl:choose>
</xsl:when>
</xsl:choose>
</xsl:for-each>
</xsl:for-each>

<xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole">
<xsl:variable name="featureRel" select="." />
<xsl:choose>
<xsl:when test="$featuretype/@id = $featureRel/inType/@idref">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Relationsart:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s52\qj \fi-1418\li1418\ri0\sa60\sl240\slmult0\widctlpar\intbl\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin1418 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031
\cell Bezeichnung:\tab <xsl:value-of select="$featureRel/name"/>\cell\row 
<xsl:variable name="nft" select="count($featureRel/code)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Kennung:\tab <xsl:value-of select="$featureRel/code"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureRel/cardinality)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Kardinalit\'e4t:\tab <xsl:value-of select="$featureRel/cardinality"/> <xsl:choose><xsl:when test="$featureRel/orderIndicator = 1">(geordnet)</xsl:when></xsl:choose>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureRel/modellart)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Modellart:\tab <xsl:value-of select="$featureRel/modellart"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureRel/grunddatenbestand)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Grunddatenb.:\tab <xsl:value-of select="$featureRel/grunddatenbestand"/>\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureRel/FeatureTypeIncluded)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Zielobjektart:\tab 
<xsl:for-each select="$featureRel/FeatureTypeIncluded">
<xsl:value-of select="@name"/>
</xsl:for-each>
\cell\row
</xsl:when>
</xsl:choose>
<xsl:variable name="nft" select="count($featureRel/InverseRole)" />
<xsl:choose>
<xsl:when test="$nft = 1">
\cell Inv. Relation:\tab 
<xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole">
<xsl:variable name="ftinc" select="." />
<xsl:choose>
<xsl:when test=" @id = $featureRel/InverseRole/@idref">
<xsl:value-of select="name"/>
</xsl:when>
</xsl:choose>
</xsl:for-each> 
\cell\row
</xsl:when>
</xsl:choose>

<xsl:for-each select="$featureRel/relation">
<xsl:variable name="rel" select="." />
<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureRelationship">
<xsl:choose>
<xsl:when test=" @id = $rel/@idref">
<xsl:variable name="nft" select="count(definition)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
\cell Definition:\tab
<xsl:for-each select="definition"><xsl:value-of select="."/>\par\tab </xsl:for-each>
\cell\row
</xsl:when>
</xsl:choose>
</xsl:when>
</xsl:choose>
</xsl:for-each>
</xsl:for-each>

<xsl:variable name="nft" select="count($featureRel/definition)+count($featureRel/objektbildend)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
\cell Anmerkung:\tab 
<xsl:for-each select="$featureRel/definition"><xsl:value-of select="."/>\par\tab </xsl:for-each><xsl:if test="$featureRel/objektbildend=true">Diese Relationsart ist objektbildend.\par\tab</xsl:if> \cell\row
</xsl:when>
</xsl:choose>

</xsl:when>
</xsl:choose>
</xsl:for-each>

<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureOperation">
<xsl:variable name="featureOpr" select="." />
<xsl:for-each select="$featuretype/characterizedBy">
<xsl:choose>
<xsl:when test=" @idref = $featureOpr/@id">

\pard\plain
\trowd \trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s54\ql \li0\ri0\sb60\sa60\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \b\fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{Methode:}
\b0\cell\row 

\pard\plain
\trowd 
\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrs\brdrw15 
\trbrdrb\brdrs\brdrw15 
\trbrdrr\brdrs\brdrw15 
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrs\brdrw15 
\clbrdrb\brdrnone 
\clbrdrr\brdrnone 
\cltxlrtb\clftsWidth3\clwWidth499 
\cellx428

\clvertalt
\clbrdrt\brdrnone 
\clbrdrl\brdrnone 
\clbrdrb\brdrnone 
\clbrdrr\brdrs\brdrw15 
\cltxlrtb\clftsWidth3\clwWidth8646 
\cellx9074

\pard\plain 
\s52\qj \fi-1418\li1418\ri0\sa60\sl240\slmult0\widctlpar\intbl\tx1418\aspalpha\aspnum\faauto\adjustright\rin0\lin1418 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031
\cell Bezeichnung:\tab <xsl:value-of select="$featureOpr/name"/>\cell\row 
<xsl:variable name="nft" select="count($featureOpr/definition)" />
<xsl:choose>
<xsl:when test="$nft >= 1">
\cell Definition:\tab 
<xsl:for-each select="$featureOpr/definition"><xsl:value-of select="."/>\par\tab </xsl:for-each>
\cell\row
</xsl:when>
</xsl:choose>
</xsl:when>
</xsl:choose>
</xsl:for-each>
</xsl:for-each>

\pard\plain
\trowd\trgaph71\trleft-71
\trbrdrt\brdrs\brdrw15 
\trbrdrl\brdrnone
\trbrdrb\brdrnone
\trbrdrr\brdrnone
\trftsWidth1\trpaddl71\trpaddr71\trpaddfl3\trpaddfr3 
\clvertalt
\clbrdrt\brdrs\brdrw15 
\clbrdrl\brdrnone
\clbrdrb\brdrnone
\clbrdrr\brdrnone
\cltxlrtb\clftsWidth3\clwWidth9145 
\cellx9074

\pard\plain 
\s51\qj \li0\ri0\sa60\sl240\slmult0\widctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0 \fs22\lang1031\langfe1031\cgrid\langnp1031\langfenp1031 
{ }\cell\row

\pard\plain
\ql \li0\ri0\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0

\par

</xsl:when> 	
</xsl:choose>
</xsl:for-each>
</xsl:for-each>
</xsl:if>
</xsl:for-each>

}
</xsl:template>

</xsl:stylesheet>
