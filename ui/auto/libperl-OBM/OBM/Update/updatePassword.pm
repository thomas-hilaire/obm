package OBM::Update::updatePassword;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
use OBM::Update::commonUpdate qw(   
            _getUserIdFromUserLoginDomain
            );


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }


    if( !$parameters->{'no-old'} ) {
        $self->{'oldPassword'} = $parameters->{'old-passwd'};
    }

    $self->{'newPasswordDesc'}->{'newPassword'} = $parameters->{'passwd'};
    $self->{'newPasswordDesc'}->{'unix'} = $parameters->{'unix'};
    $self->{'newPasswordDesc'}->{'samba'} = $parameters->{'samba'};
    $self->{'newPasswordDesc'}->{'sql'} = $parameters->{'sql'};

    $self->{'userLogin'} = $parameters->{'login'};
    $self->{'domainId'} = $parameters->{'domain'};

    require OBM::Ldap::ldapServers;
    if( !($self->{'ldapservers'} = OBM::Ldap::ldapServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur LDAP impossible', 3 );
        return undef;
    }

    if( $self->_getEntity() ) {
        return undef;
    }

    return $self;
}


sub _getEntity {
    my $self = shift;

    # Getting BD user ID
    my $userId = $self->_getUserIdFromUserLoginDomain( $self->{'userLogin'}, $self->{'domainId'} );
    if( !defined($userId) ) {
        $self->_log( 'utilisateur \''.$self->{'userLogin'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting user entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    $programmingObj->setEntitiesType( 'USER' );
    $programmingObj->setUpdateType( 'SYSTEM_ENTITY' );
    $programmingObj->setEntitiesIds( [$userId] );

    require OBM::entitiesFactory;
    my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLE', 1 );
    if( !defined($entitiesFactory) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $entitiesFactory->loadEntities($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    while( my $userEntity = $entitiesFactory->next() ) {
        $self->{'userEntity'} = $userEntity;
    }

    if( !defined($self->{'userEntity'}) ) {
        $self->_log( 'problème lors de la récupération de la description de l\'utilisateur', 0 );
        return 1;
    }

    return 0;
}


sub update {
    my $self = shift;

    my $entityDns = $self->{'userEntity'}->getCurrentDnPrefix();

    if( $self->{oldPassword} ) {
        if( $self->_checkPasswd() ) {
            $self->_log( 'erreur lors de la vérification de l\'ancien mot de passe de '.$self->{'userEntity'}->getDescription(), 0 );
            return 1;
        }else {
            $self->_log( 'ancien mot de passe correct', 0 );
        }
    }

    if( $self->_updatePassword() ) {
        $self->_log( 'erreur lors de la mise a jour du mot de passe de '.$self->{'userEntity'}->getDescription(), 0 );
        return 1;
    }
#    if( !$ldapEngine->updatePassword( $userObject, $self->{newPasswordDesc} ) ) {
#        return 0;
#    }
#
#
#    if( !$userObject->updateDbEntityPassword( $self->{newPasswordDesc} ) ) {
#        return 0;
#    }

    return 0;
}


sub _checkPasswd {
    my $self = shift;

    my $entityLdapServer = $self->{'ldapservers'}->getLdapServer($self->{'userEntity'}->getLdapServerId());
    if( ref($entityLdapServer) ne 'OBM::Ldap::ldapServer' ) {
        $self->_log( 'erreur lors de l\'obtention du serveur LDAP de '.$self->{'userEntity'}->getDescription(), 3 );
        return 1;
    }

    my $currentEntityDNs = $self->{'userEntity'}->getCurrentDnPrefix();
    if( !defined($currentEntityDNs) ) {
        $self->_log( 'erreur lors de l\'obtention du DN de '.$self->{'userEntity'}->getDescription(), 3 );
        return 1;
    }

    for( my $i=0; $i<=$#{$currentEntityDNs}; $i++ ) {
        $entityLdapServer->resetConn();

        $entityLdapServer->setDnLogin( $currentEntityDNs->[$i] );
        $entityLdapServer->setPasswd( $self->{oldPassword} );

        if( !$entityLdapServer->getLdapConn() ) {
            $entityLdapServer->resetConn();
            return 1;
        }
    }

    $entityLdapServer->resetConn();
    return 0;
}


sub _updatePassword {
    my $self = shift;

    if( $self->{'newPasswordDesc'}->{'unix'} ) {
        $self->_log( 'mise à jour du mot de passe Unix', 4 );

        if( $self->_updateUnixPasswd() ) {
            $self->_log( 'probleme lors de la mise à jour du mot de passe Unix', 3 );
            return 1;
        }else {
            $self->_log( 'mise à jour du mot de passe Unix', 2 );
        }
    }
#    $self->{'newPasswordDesc'}->{'samba'} = $parameters->{'samba'};
#    $self->{'newPasswordDesc'}->{'sql'} = $parameters->{'sql'};


    return 0;
}


sub _updateUnixPasswd {
    my $self = shift;

    require OBM::Password::unixPasswdUpdater;
    my $passwordUpdater = OBM::Password::unixPasswdUpdater->new();
    if( $passwordUpdater->update( $self->{'userEntity'}, $self->{'newPasswordDesc'}->{'newPassword'} ) ) {
        $self->_log( 'problème a la mise à jour du mot de passe Unix', 0 );
        return 1;
    }

    return 0;
}
