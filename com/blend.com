# Command file to run BLENDMONT
#
$ blendmont -StandardInput
ImageInputFile		g5a.st
PieceListInput		g5a.pl
ImageOutputFile		g5a.ali
RootNameForEdges	g5a
TransformFile		g5a.xf
#SloppyMontage
#ShiftPieces
#ReadInXcorrs
#OldEdgeFunctions
StartingAndEndingX	/
StartingAndEndingY	/
$mrctaper g5a.ali
