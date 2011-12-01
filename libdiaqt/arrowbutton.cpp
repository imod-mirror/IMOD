// A simple class to incorporate nice arrow icons into tool buttons

#include "arrowbutton.h"
#include <qpixmap.h>
#include <qicon.h>

static const char* const left_data[] = { 
"13 13 78 2",
".H c #000000",
".5 c #010101",
".M c #020202",
".X c #040404",
".O c #050505",
".N c #060606",
".G c #070707",
".Y c #080808",
".4 c #090909",
".W c #0a0a0a",
".9 c #101010",
".V c #121212",
"#d c #1f1f1f",
"#g c #585858",
".A c #5c5c5c",
"#. c #5d5d5d",
"#e c #5f5f5f",
"#b c #626262",
"#c c #636363",
".8 c #666666",
".R c #676767",
".Q c #686868",
".2 c #6a6a6a",
".z c #6c6c6c",
".P c #6d6d6d",
".s c #6e6e6e",
".J c #6f6f6f",
".Z c #717171",
".6 c #727272",
"#j c #747474",
".3 c #757575",
".I c #777777",
".1 c #797979",
"#l c #7d7d7d",
".0 c #a6a6a6",
"#a c #b0b0b0",
"#h c #b7b7b7",
".f c #b9b9b9",
".B c #bababa",
"## c #bbbbbb",
"#k c #bcbcbc",
".c c #bdbdbd",
".m c #bebebe",
".l c #bfbfbf",
"Qt c #c0c0c0",
".t c #c1c1c1",
".n c #c2c2c2",
".# c #c3c3c3",
".j c #c4c4c4",
".o c #c5c5c5",
".g c #c7c7c7",
".k c #c8c8c8",
".b c #c9c9c9",
".u c #cacaca",
".a c #cbcbcb",
"#i c #cccccc",
".d c #cdcdcd",
".C c #d1d1d1",
".e c #d2d2d2",
".K c #d3d3d3",
"#f c #d4d4d4",
".7 c #d5d5d5",
".x c #dadada",
".v c #dbdbdb",
".q c #dcdcdc",
".h c #dedede",
".E c #e2e2e2",
".p c #e3e3e3",
".S c #e4e4e4",
".F c #e5e5e5",
".D c #e6e6e6",
".T c #e7e7e7",
".U c #e8e8e8",
".y c #e9e9e9",
".r c #ebebeb",
".i c #ececec",
".L c #f1f1f1",
".w c #ffffff",
"Qt.#.a.b.a.c.d.e.f.d.g.h.i",
".j.k.l.m.n.m.o.n.l.p.q.r.s",
".t.n.g.d.m.d.u.v.w.x.y.z.A",
".j.j.B.C.B.D.E.F.q.G.H.I.J",
".#.l.g.K.L.y.F.M.N.G.O.P.P",
".C.E.p.w.x.H.H.H.H.H.H.Q.R",
".S.T.U.H.H.V.V.W.X.Y.M.Z.s",
".0.1.2.3.P.H.H.H.4.H.5.J.6",
".7.a.#.R.8.6.6.H.9.H.H#..2",
"###a.#.k.g#b.3.R#c.H#d#e.z",
".K.dQtQt#f.l.f.J.J.J#g.P.Z",
".tQt.b.j#h.d.o.B#i.z#j.z.2",
".k.o.#.#.#.#.#.#.#.##k#l.J"};

static const char* const right_data[] = { 
"13 13 91 2",
".L c #000000",
".0 c #010101",
".V c #020202",
"#d c #030303",
".T c #040404",
".K c #050505",
".D c #060606",
".S c #070707",
".U c #090909",
".C c #0b0b0b",
".6 c #131313",
".1 c #1a1a1a",
"#e c #565656",
"#b c #5b5b5b",
".9 c #5c5c5c",
"#r c #616161",
"#j c #636363",
".2 c #656565",
"#i c #676767",
"#l c #6a6a6a",
".7 c #6d6d6d",
"#f c #6e6e6e",
"#s c #727272",
".8 c #737373",
".4 c #747474",
"#a c #767676",
".3 c #777777",
"#q c #7a7a7a",
"## c #7d7d7d",
"#k c #878787",
"#o c #a8a8a8",
"#p c #aeaeae",
".a c #afafaf",
"#x c #b2b2b2",
".r c #b6b6b6",
"#g c #b7b7b7",
"#m c #b8b8b8",
".g c #b9b9b9",
".d c #bababa",
".P c #bbbbbb",
"#t c #bcbcbc",
".I c #bdbdbd",
".m c #bebebe",
".G c #bfbfbf",
".f c #c0c0c0",
".c c #c1c1c1",
".s c #c2c2c2",
"#y c #c3c3c3",
".n c #c4c4c4",
".h c #c5c5c5",
".o c #c6c6c6",
"#v c #c7c7c7",
".y c #c8c8c8",
"#. c #c9c9c9",
".q c #cbcbcb",
".# c #cccccc",
".z c #cdcdcd",
"#u c #cecece",
".H c #cfcfcf",
".e c #d0d0d0",
".b c #d1d1d1",
"#c c #d2d2d2",
".j c #d3d3d3",
".A c #d4d4d4",
".p c #d5d5d5",
".E c #d6d6d6",
"#n c #d7d7d7",
".Y c #d8d8d8",
".u c #d9d9d9",
"#h c #dbdbdb",
".O c #dcdcdc",
".Z c #dddddd",
".X c #dedede",
".Q c #dfdfdf",
".v c #e1e1e1",
"#w c #e2e2e2",
".w c #e3e3e3",
".N c #e4e4e4",
".J c #e6e6e6",
".F c #e8e8e8",
".i c #e9e9e9",
"Qt c #eaeaea",
".l c #ececec",
".M c #eeeeee",
".k c #efefef",
".B c #f1f1f1",
".x c #f2f2f2",
".5 c #f3f3f3",
".R c #f5f5f5",
".t c #f7f7f7",
".W c #ffffff",
"QtQt.#.a.b.c.d.e.f.g.h.#.h",
".i.j.k.l.m.n.m.o.p.q.r.s.m",
".t.l.u.v.w.x.y.s.r.c.c.z.o",
".A.B.C.D.i.E.i.F.G.H.q.I.n",
".J.u.K.L.L.K.M.N.N.O.A.c.P",
".Q.R.S.T.U.L.V.S.E.W.X.Y.#",
".Z.E.L.L.0.T.K.L.1.L.2.3.4",
".5.F.6.L.C.V.0.K.7.8.4.9#.",
".X.B.T.V.S.L##.2#a#b.y.c.o",
"#c.F#d.6#e#a#f.9.h.E.d#g#h",
".i.M#i#j#k#l#m#n.m#o.e.H#p",
".X#q#r#s.n.s#t.m.y.H.G#u#v",
"#w#a#x.I.n#y.s#v.z#y#g.P.b"};

static const char* const up_data[] = { 
"13 13 89 2",
".L c #000000",
".5 c #010101",
".G c #020202",
".T c #040404",
".6 c #050505",
".S c #070707",
"#d c #090909",
"#h c #0a0a0a",
"#b c #0b0b0b",
"#c c #0d0d0d",
"#g c #0f0f0f",
".0 c #191919",
".U c #1e1e1e",
".I c #575757",
".M c #5b5b5b",
".V c #5c5c5c",
"#t c #5e5e5e",
"#j c #646464",
"#q c #656565",
"#e c #666666",
".1 c #676767",
"## c #686868",
"#i c #696969",
".z c #6a6a6a",
".N c #6b6b6b",
"#r c #6c6c6c",
".W c #6e6e6e",
"#n c #6f6f6f",
"#s c #717171",
"#w c #727272",
"#v c #737373",
"#u c #747474",
"#p c #757575",
"#o c #777777",
"#f c #787878",
".p c #7c7c7c",
".H c #7d7d7d",
".7 c #858585",
".8 c #a6a6a6",
".k c #ababab",
".g c #acacac",
".P c #afafaf",
".Y c #b4b4b4",
".r c #b5b5b5",
".i c #b6b6b6",
"#k c #b7b7b7",
".w c #b8b8b8",
".x c #bababa",
".u c #bcbcbc",
"Qt c #bdbdbd",
".b c #bebebe",
".A c #c0c0c0",
".D c #c1c1c1",
".C c #c2c2c2",
".s c #c3c3c3",
".# c #c4c4c4",
".B c #c5c5c5",
".v c #c6c6c6",
".q c #c7c7c7",
".c c #c8c8c8",
".d c #c9c9c9",
".f c #cacaca",
".t c #cccccc",
".O c #cdcdcd",
".j c #cecece",
".m c #cfcfcf",
".X c #d0d0d0",
".e c #d2d2d2",
".a c #d4d4d4",
".n c #d5d5d5",
"#l c #d6d6d6",
".E c #d8d8d8",
".y c #d9d9d9",
".2 c #dadada",
".l c #dbdbdb",
".9 c #dddddd",
"#a c #dedede",
".h c #dfdfdf",
".J c #e0e0e0",
".K c #e2e2e2",
"#. c #e3e3e3",
".F c #e6e6e6",
".Z c #eaeaea",
".3 c #ededed",
".R c #eeeeee",
".o c #f0f0f0",
".4 c #f5f5f5",
".Q c #f8f8f8",
"#m c #fafafa",
"Qt.#.a.b.c.d.e.f.g.h.i.#.b",
".c.j.k.l.m.n.o.p.q.r.s.t.u",
".v.w.w.f.x.o.y.z.t.A.B.c.C",
".D.C.E.s.y.F.G.H.I.m.c.A.i",
".b.e.f.#.J.K.L.M.N.t.d.b.O",
".t.u.P.Q.R.S.T.U.V.W.D.s.A",
".u.X.Y.F.Z.S.0.L.N.1.P.2.d",
".C.D.3.4.5.L.6.T.S.z.7.a.8",
".q.m.9#..L.L.6.L.S####.P.u",
".j#a.J#b.L#c.T.S.L#d#e#f#.",
".c.J.3.L#g.L.L.L#h#h#i#j#k",
"#l#m.W#n#o#i#i#p#q#r#s.z#j",
"#.#t#r###u#s.z#q#n#e.1#v#w"};

static const char* const down_data[] = { 
"13 13 89 2",
".w c #000000",
".F c #010101",
".y c #020202",
".x c #040404",
".W c #050505",
".4 c #070707",
".O c #090909",
".P c #0a0a0a",
".G c #0c0c0c",
"#. c #0d0d0d",
".H c #0e0e0e",
".X c #0f0f0f",
".v c #121212",
".z c #161616",
"#e c #181818",
"#k c #4d4d4d",
".A c #505050",
"#f c #595959",
".5 c #5a5a5a",
".Z c #626262",
"## c #6a6a6a",
".Q c #6b6b6b",
"#r c #6c6c6c",
"#o c #6d6d6d",
".J c #6e6e6e",
".R c #6f6f6f",
".Y c #707070",
".B c #737373",
".r c #767676",
".q c #787878",
".I c #7c7c7c",
"#j c #7d7d7d",
"#g c #828282",
".2 c #afafaf",
"#l c #b1b1b1",
".C c #b3b3b3",
"#u c #b4b4b4",
"#s c #b7b7b7",
"#c c #b8b8b8",
"#t c #b9b9b9",
"#m c #bababa",
".7 c #bbbbbb",
".S c #bdbdbd",
".U c #bebebe",
".T c #bfbfbf",
"#q c #c1c1c1",
".L c #c2c2c2",
".K c #c3c3c3",
".s c #c4c4c4",
"#a c #c5c5c5",
"#h c #c6c6c6",
".D c #c7c7c7",
".M c #c8c8c8",
"#v c #c9c9c9",
".N c #cacaca",
".6 c #cbcbcb",
".0 c #cccccc",
".1 c #cdcdcd",
".i c #cecece",
"#w c #cfcfcf",
"#b c #d2d2d2",
".8 c #d3d3d3",
"#p c #d5d5d5",
".u c #d6d6d6",
".k c #d7d7d7",
"#i c #d8d8d8",
".9 c #d9d9d9",
"Qt c #dadada",
".t c #dbdbdb",
".o c #dcdcdc",
".g c #dedede",
".a c #dfdfdf",
".n c #e1e1e1",
".c c #e2e2e2",
".e c #e3e3e3",
"#d c #e5e5e5",
".h c #e6e6e6",
".l c #e7e7e7",
".# c #e9e9e9",
".d c #eaeaea",
".f c #ebebeb",
"#n c #ececec",
".m c #efefef",
".b c #f1f1f1",
".3 c #f4f4f4",
".p c #f8f8f8",
".V c #f9f9f9",
".j c #fefefe",
".E c #ffffff",
"Qt.#.a.b.c.d.e.f.g.h.a.#.i",
".h.f.j.k.l.m.n.h.f.o.p.q.r",
".s.t.u.v.w.w.x.y.w.z.A.B.C",
".D.c.E.w.F.w.G.w.H.w.I.J.K",
".L.M.N.j.O.w.y.P.w.Q.R.S.T",
".U.D.V.u.W.w.z.w.X.Y.Z.s.0",
".U.1.2.3.g.4.w.G.5.J.6.T.K",
".7.s.8.9.d.v.w#..R##.L#a.K",
"#b#c.7.L#dQt#e#f#g.0.L.s.k",
"#h.L.M#i#d.g.F#j#k.N#c#l.T",
".0.M.C#m.0#n#o###p.U.g.1.s",
".S#q.M#h.6.e#r#r#h#s.S#t.K",
".s.T.8#s#u.7.q#v#u#w.K.U.N"};

/*!
 * A toolbutton with an arrow icon.  [type] should be one of Qt::UpArrow,
 * Qt::DownArrow, Qt::LeftArrow, Qt::RightArrow.  [name] defaults to 0.
 */
ArrowButton::ArrowButton ( Qt::ArrowType type, QWidget * parent, 
                           const char * name) 
  : QToolButton(parent)
{
  const char **data;
  switch (type) {
  case Qt::LeftArrow:
    data = ( const char** ) left_data;
    break;
  case Qt::RightArrow:
    data = (const char** ) right_data;
    break;
  case Qt::UpArrow:
    data = ( const char** ) up_data;
    break;
  case Qt::DownArrow:
    data = ( const char** ) down_data;
    break;
  }
  setText( trUtf8( "" ) );
  QPixmap image(data);
  setIcon(QIcon(image));
}

ArrowButton::~ArrowButton()
{
}
