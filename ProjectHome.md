# DAS Writeback #
We designed and developed a Collaborative Annotation System for protein sequence information called DAS Writeback, which extends the Distributed Annotation System ([DAS](http://www.biodas.org)) to provide the functionalities of adding, editing and deleting annotations.
<p align='center'>
<img src='http://writeback.googlecode.com/files/WritebackArchitecture.png' width='50%' />
</p>

The solution consists of an extension of the [DAS protocol](http://www.biodas.org/wiki/DAS1.6E#DAS_writeback) that defines the communication rules between the client and the writeback server following the Uniform Interface of the RESTful architecture. A protocol extension was proposed to the DAS community and implementations of both server and client were created in order to have a fully functional system. For the development of the server, writing functionalities were added to [MyDAS](http://code.google.com/p/mydas/), which is a widely used DAS server. The writeback client is an extended version of the web-based protein client [Dasty3](http://code.google.com/p/dasty/), which can be used in its instance at http://www.ebi.ac.uk/dasty/.

A tutorial to install and execute the main commands of the Writeback data source can be found [here](http://code.google.com/p/writeback/wiki/Tutorial).

The live installation of the writeback for the protein coordinate system is in http://www.ebi.ac.uk/enfin-srv/wb-srv/das/writeback.

The DAS writeback can be cited as:
Gustavo A Salazar, Rafael C Jimenez, Alexander Garcia, Henning Hermjakob, Nicola Mulder and Edwin Blake: **DAS Writeback: A Collaborative Annotation System**. http://www.biomedcentral.com/1471-2105/12/143/
BMC Bioinformatics 2011, 12:143

A previous related project inspired in DAS/2 can be found [here](http://code.google.com/p/daswriteback).