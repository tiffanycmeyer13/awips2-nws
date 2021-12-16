# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

Name: awips2-tsunamieasgui
Summary: awips2-tsunamieasgui Installation
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

Provides: awips2-tsunamieasgui
Requires: awips2
Requires: awips2-apps
Requires: awips2-python

%description
AWIPS II TsunamiEasGui - Installs AWIPS II TsunamiEasGui Application.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "%{_build_root}" = "" ]
then
   echo "A Build Root has not been specified."
   echo "Unable To Continue ... Terminating"
   exit 1
fi

# Clean build root and create a new directory
rm --recursive --force %{_build_root}
mkdir --parents %{_build_root}

%install
APP_DIR="%{_build_root}/awips2/apps/TsunamiEasGui"
mkdir --parents ${APP_DIR}
mkdir --parents ${APP_DIR}/etc
mkdir --parents ${APP_DIR}/log
SRC_DIR="%{_baseline_workspace}/apps/TsunamiEasGui"
rsync --archive ${SRC_DIR}/ ${APP_DIR}/

mkdir --parents %{_build_root}/usr/share/applications

DESKTOP_ITEMS=%{_baseline_workspace}/rpms-nws/Installer.TsunamiEasGui/scripts
cp ${DESKTOP_ITEMS}/* %{_build_root}/usr/share/applications
if [ $? -ne 0 ]; then
   exit 1
fi

%post
# Copy Config files only on initial install.
if [ $1 == 1 ];then
   /awips2/apps/TsunamiEasGui/bin/copyTsunamiGui.sh
fi

%clean
rm --recursive --force %{_build_root}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/apps/TsunamiEasGui 
%dir /awips2/apps/TsunamiEasGui/bin
%attr(755,awips,fxalpha) /awips2/apps/TsunamiEasGui/bin/copyTsunamiGui.sh
%attr(755,awips,fxalpha) /awips2/apps/TsunamiEasGui/bin/modecheck.sh
%attr(755,awips,fxalpha) /awips2/apps/TsunamiEasGui/bin/nwrsend.sh
%attr(755,awips,fxalpha) /awips2/apps/TsunamiEasGui/bin/nwr_tsu.py
%dir /awips2/apps/TsunamiEasGui/config
/awips2/apps/TsunamiEasGui/config/*
%dir /awips2/apps/TsunamiEasGui/docs
/awips2/apps/TsunamiEasGui/docs/*
%dir /awips2/apps/TsunamiEasGui/examples
/awips2/apps/TsunamiEasGui/examples/*
%dir /awips2/apps/TsunamiEasGui/etc
%dir /awips2/apps/TsunamiEasGui/log

%defattr(644,root,root,-)
/usr/share/applications/TsunamiEasGui.desktop
/usr/share/applications/tsunami_icon.jpg

%changelog
* Thu Dec 9 2021 David Lovely <david.lovely@raytheon.com> 
- Initial package creation.
