package OBM::Entities::obmMailshare;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::toolBox;
require OBM::dbUtils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted, $mailShareId ) = @_;

    my %obmMailshareAttr = (
        type => undef,
        entityRightType => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        mailShareId => undef,
        domainId => undef,
        mailShareDesc => undef,
        mailShareDbDesc => undef
    );


    if( !defined( $links ) || !defined($deleted) || !defined($mailShareId) ) {
        croak( "Usage: PACKAGE->new(LINKS, MAILSHAREID)" );

    }elsif( $mailShareId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "obmMailshare: identifiant de BAL partagee incorrect", "W" );
        return undef;

    }else {
        $obmMailshareAttr{"mailShareId"} = $mailShareId;

    }


    $obmMailshareAttr{"links"} = $links;
    $obmMailshareAttr{"toDelete"} = $deleted;

    $obmMailshareAttr{"type"} = $MAILSHARE;
    $obmMailshareAttr{"entityRightType"} = "MailShare";
    $obmMailshareAttr{"typeDesc"} = $attributeDef->{$obmMailshareAttr{"type"}};
    $obmMailshareAttr{"archive"} = 0;
    $obmMailshareAttr{"sieve"} = 0;

    bless( \%obmMailshareAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $mailShareId = $self->{"mailShareId"};


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "obmMailshare: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "obmMailshare: description de domaine OBM incorrecte", "W" );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $mailShareTable = "MailShare";
    my $mailServerTable = "MailServer";
    if( $self->getDelete() ) {
        $mailShareTable = "P_".$mailShareTable;
        $mailServerTable = "P_".$mailServerTable;
    }

    my $query = "SELECT COUNT(*) FROM ".$mailShareTable." LEFT JOIN ".$mailServerTable." ON mailshare_mail_server_id=mailserver_id WHERE mailshare_id=".$mailShareId;

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "obmMailshare: pas de BAL partage d'identifiant : ".$mailShareId, "W" );
        return 0;
    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "obmMailshare: plusieurs BAL partages d'identifiant : ".$mailShareId." ???", "W" );
        return 0;
    }


    # La requete a executer - obtention des informations sur le répertoire
    # partagé
    $query = "SELECT * FROM ".$mailShareTable." LEFT JOIN ".$mailServerTable." ON mailshare_mail_server_id=mailserver_id WHERE mailshare_id=".$mailShareId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbMailShareDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();

    # On stocke la description BD utile pour la MAJ des tables
    $self->{"mailShareDbDesc"} = $dbMailShareDesc;

    if( $self->getDelete() ) {
        &OBM::toolBox::write_log( "obmMailshare: suppression de la BAL partagee : '".$dbMailShareDesc->{"mailshare_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
    
    }else {
        &OBM::toolBox::write_log( "obmMailshare: gestion de la BAL partagee : '".$dbMailShareDesc->{"mailshare_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );

    }

    # On range les resultats dans la structure de donnees des resultats
    $self->{"mailShareDesc"}->{"mailshare_name"} = $dbMailShareDesc->{"mailshare_name"};
    $self->{"mailShareDesc"}->{"mailshare_mailbox"} = "+".$dbMailShareDesc->{"mailshare_name"}."@".$domainDesc->{"domain_name"};
    $self->{"mailShareDesc"}->{"mailshare_description"} = $dbMailShareDesc->{"mailshare_description"};
    $self->{"mailShareDesc"}->{"mailshare_domain"} = $domainDesc->{"domain_label"};

    if( $dbMailShareDesc->{"mailshare_email"} ) {
        my $localServerIp = $self->getHostIpById( $dbHandler, $dbMailShareDesc->{"mailserver_host_id"} );

        if( !defined($localServerIp) ) {
            &OBM::toolBox::write_log( "obmMailshare: droit mail du repertoire partage : '".$dbMailShareDesc->{"mailshare_name"}."' annule - Serveur inconnu !", "W" );
            $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 0;

        }else {
            $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 1;
            push( @{$self->{"mailShareDesc"}->{"mailshare_mail"}}, $dbMailShareDesc->{"mailshare_email"}."@".$domainDesc->{"domain_name"} );

            for( my $j=0; $j<=$#{$domainDesc->{"domain_alias"}}; $j++ ) {
                push( @{$self->{"mailShareDesc"}->{"mailshare_mail_alias"}}, $dbMailShareDesc->{"mailshare_email"}."@".$domainDesc->{"domain_alias"}->[$j] );
            }

            # Gestion de la BAL destination
            $self->{"mailShareDesc"}->{"mailShare_mailbox"} = $self->{"mailShareDesc"}->{"mailshare_name"}."@".$domainDesc->{"domain_name"};

            # On ajoute le serveur de mail associé
            $self->{"mailShareDesc"}->{"mailShare_mailLocalServer"} = "lmtp:".$localServerIp.":24";

            # Gestion du serveur de mail
            $self->{"mailShareDesc"}->{"mailShare_server"} = $dbMailShareDesc->{"mailserver_host_id"};

            # Gestion du quota
            $self->{"mailShareDesc"}->{"user_mailbox_quota"} = $dbMailShareDesc->{"mailshare_quota"}*1000;
        }

    }else {
        $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 0;

    }

    # On positionne l'identifiant du domaine de l'entité
    $self->{"domainId"} = $domainDesc->{"domain_id"};

    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->isLinks() ) {
        $self->getEntityLinks( $dbHandler, $domainDesc );
    }


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    my( $dbHandler ) = @_;

    if( !defined($dbHandler) ) {
        return 0;
    }

    my $dbMailShareDesc = $self->{"mailShareDbDesc"};
    if( !defined($dbMailShareDesc) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "obmMailshare: MAJ de la boite a lettre partagee '".$dbMailShareDesc->{"mailshare_name"}."' dans les tables de production", "W" );

    # MAJ de l'entité dans la table de production
    my $query = "DELETE FROM P_MailShare WHERE mailshare_id=".$self->{"mailShareId"};
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Obtention des noms de colonnes de la table
    $query = "SELECT * FROM P_MailShare WHERE 0=1";
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }
    my $columnList = $queryResult->{NAME};

    $query = "INSERT INTO P_MailShare SET ";
    my $first = 1;
    for( my $i=0; $i<=$#$columnList; $i++ ) {
        if( !$first ) {
            $query .= ", ";
        }else {
            $first = 0;
        }

        $query .= $columnList->[$i]."=".$dbHandler->quote($dbMailShareDesc->{$columnList->[$i]});
    }

    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return 0;
    }

    # Les liens
    if( $self->isLinks() ) {
        # On supprime les liens actuels de la table de production
        $query = "DELETE FROM P_EntityRight WHERE entityright_entity_id=".$self->{"mailShareId"}." AND entityright_entity='".$self->{"entityRightType"}."'";

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }


        # On copie les nouveaux droits
        $query = "SELECT * FROM EntityRight WHERE entityright_entity='".$self->{"entityRightType"}."' AND entityright_entity_id=".$self->{"mailShareId"};

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
            return 0;
        }

        while( my $rowHash = $queryResult->fetchrow_hashref() ) {
            $query = "INSERT INTO P_EntityRight SET ";

            my $first = 1;
            while( my( $column, $value ) = each(%{$rowHash}) ) {
                if( !$first ) {
                    $query .= ", ";
                }else {
                    $first = 0;
                }

                $query .= $column."=".$dbHandler->quote($value);
            }

            my $queryResult2;
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
                &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
                return 0;
            }
        }
    }

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    $self->_getEntityMailShareAcl( $dbHandler, $domainDesc );

    # On précise que les liens de l'entité sont aussi à mettre à jour.
    $self->{"links"} = 1;

    return 1;
}


sub setDelete {
    my $self = shift;

    $self->{"toDelete"} = 1;

    return 1;
}


sub getDelete {
    my $self = shift;

    return $self->{"toDelete"};
}


sub getArchive {
    my $self = shift;

    return $self->{"archive"};
}


sub isLinks {
    my $self = shift;

    return $self->{"links"};
}


sub _getEntityMailShareAcl {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $mailShareId = $self->{"mailShareId"};

    if( !$self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $self->{"mailShareDesc"}->{"user_mailshare_acl"} = undef;

    }else {
        my $entityType = $self->{"entityRightType"};
        my %rightDef;


        my $userObmTable = "UserObm";
        my $entityRightTable = "EntityRight";
        if( $self->getDelete() ) {
            $userObmTable = "P_".$userObmTable;
            $entityRightTable = "P_".$entityRightTable;
        }

        $rightDef{"read"}->{"compute"} = 1;
        $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM ".$userObmTable." i, ".$entityRightTable." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"writeonly"}->{"compute"} = 1;
        $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM ".$userObmTable." i, ".$entityRightTable." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"write"}->{"compute"} = 1;
        $rightDef{"write"}->{"sqlQuery"} = "SELECT i.userobm_id, i.userobm_login FROM ".$userObmTable." i, ".$entityRightTable." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=1 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"public"}->{"compute"} = 0;
        $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM ".$entityRightTable." WHERE entityright_entity_id=".$mailShareId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

        # On recupere la definition des ACL
        $self->{"mailShareDesc"}->{"user_mailshare_acl"} = &OBM::toolBox::getEntityRight( $dbHandler, $domainDesc, \%rightDef, $mailShareId );
    }

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"mailShareDesc"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"mailShareDesc"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"mailShareDesc"};

    # Les parametres necessaires
    if( $entry->{"mailshare_name"} ) {
        $ldapEntry->add(
            objectClass => $self->{"typeDesc"}->{"objectclass"},
            cn => $entry->{"mailshare_name"},
            mailBox => $entry->{"mailshare_mailbox"}
        );

    }else {
        return 0;
    }

    if( $entry->{"mailshare_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"mailshare_description"}, -charset => $defaultCharSet }) );
    }

    # Le serveur de BAL local
    if( $entry->{"mailShare_mailLocalServer"} ) {
        $ldapEntry->add( mailBoxServer => $entry->{"mailShare_mailLocalServer"} );
    }

    # Les adresses mails
    if( $entry->{"mailshare_mail"} ) {
        $ldapEntry->add( mail => $entry->{"mailshare_mail"} );
    }

    # Les adresses mails secondaires
    if( $entry->{"mailshare_mail_alias"} ) {
        $ldapEntry->add( mailAlias => $entry->{"mailshare_mail_alias"} );
    }

    # L'acces mail
    if( $entry->{"mailshare_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # Le domaine
    if( $entry->{"mailshare_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"mailshare_domain"}, -charset => $defaultCharSet }) );
    }


    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"mailShareDesc"};
    my $update = 0;

    # Le nom de la BAL
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_mailbox"}, $ldapEntry, "mailbox" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail_alias"}, $ldapEntry, "mailAlias" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    # Le serveur de BAL local
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailShare_mailLocalServer"}, $ldapEntry, "mailBoxServer" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
    }

    return $update;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub getMailServerRef {
    my $self = shift;
    my( $domainId, $mailServerId ) = @_;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $$domainId = $self->{"domainId"};
        $$mailServerId = $self->{"mailShareDesc"}->{"mailShare_server"};

    }else {
        $$domainId = undef;
        $$mailServerId = undef;

    }

    return 1;
}


sub getMailboxPrefix {
    my $self = shift;

    return "";
}


sub getMailboxName {
    my $self = shift;
    my $mailShareName = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareName = $self->{"mailShareDesc"}->{"mailShare_mailbox"};
    }

    return $mailShareName;
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}


sub getMailboxQuota {
    my $self = shift;
    my $mailShareQuota = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareQuota = $self->{"mailShareDesc"}->{"user_mailbox_quota"};
    }

    return $mailShareQuota;
}


sub getMailboxAcl {
    my $self = shift;
    my $mailShareAcl = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareAcl = $self->{"mailShareDesc"}->{"user_mailshare_acl"};
    }

    return $mailShareAcl;
}


sub getHostIpById {
    my $self = shift;
    my( $dbHandler, $hostId ) = @_;

    if( !defined($hostId) ) {
        &OBM::toolBox::write_log( "Identifiant de l'hote non défini !", "W" );
        return undef;
    }elsif( $hostId !~ /^[0-9]+$/ ) {
        &OBM::toolBox::write_log( "Identifiant de l'hote '".$hostId."' incorrect !", "W" );
        return undef;
    }elsif( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "Connection à la base de donnee incorrect !", "W" );
        return undef;
    }

    my $hostTable = "Host";
    if( $self->getDelete() ) {
        $hostTable = "P_".$hostTable;
    }

    my $query = "SELECT host_ip FROM ".$hostTable." WHERE host_id='".$hostId."'";

    #
    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return undef;
    }

    if( !(my( $hostIp ) = $queryResult->fetchrow_array) ) {
        &OBM::toolBox::write_log( "Identifiant de l'hote '".$hostId."' inconnu !", "W" );

        $queryResult->finish;
        return undef;
    }else{
        $queryResult->finish;
        return $hostIp;
    }

    return undef;

}
