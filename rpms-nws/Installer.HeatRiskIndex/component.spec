# Turn off the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')

Name: awips2-heatriskindex
Summary: awips2-heatriskindex Installation
Version: %{_component_version}
Release: %{_component_release}
Group: AWIPSII
BuildRoot: %{_build_root}
URL: N/A
License: N/A
Distribution: N/A
Vendor: Raytheon
Packager: %{_build_site}

Provides: awips2-heatriskindex
Requires: awips2
Requires: awips2-apps

%description
AWIPS II Heat Risk Index - Installs AWIPS II HeatRiskIndex Application.

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
APP_DIR="%{_build_root}/awips2/apps/HeatRiskIndex"
mkdir --parents ${APP_DIR}
mkdir --parents ${APP_DIR}/data
mkdir --parents ${APP_DIR}/install
mkdir --parents ${APP_DIR}/runtime
mkdir --parent ${APP_DIR}/install/PrismHiRes/logs
mkdir --parent ${APP_DIR}/install/HeatRisk/logs
SRC_DIR="%{_baseline_workspace}/apps/HeatRiskIndex"
rsync --archive ${SRC_DIR}/ ${APP_DIR}/

%clean
rm --recursive --force %{_build_root}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/apps/HeatRiskIndex
%dir /awips2/apps/HeatRiskIndex/install
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/bin
/awips2/apps/HeatRiskIndex/install/HeatRisk/bin/*
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/config
/awips2/apps/HeatRiskIndex/install/HeatRisk/config/*
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/data
/awips2/apps/HeatRiskIndex/install/HeatRisk/data/*
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/etc
/awips2/apps/HeatRiskIndex/install/HeatRisk/etc/*
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/gfe
/awips2/apps/HeatRiskIndex/install/HeatRisk/gfe/*
%dir /awips2/apps/HeatRiskIndex/install/HeatRisk/install
/awips2/apps/HeatRiskIndex/install/HeatRisk/install/*
%dir /awips2/apps/HeatRiskIndex/install/PrismHiRes
%dir /awips2/apps/HeatRiskIndex/install/PrismHiRes/bin
/awips2/apps/HeatRiskIndex/install/PrismHiRes/bin/*
%dir /awips2/apps/HeatRiskIndex/install/PrismHiRes/etc
/awips2/apps/HeatRiskIndex/install/PrismHiRes/etc/*
%dir /awips2/apps/HeatRiskIndex/install/PrismHiRes/gfe
/awips2/apps/HeatRiskIndex/install/PrismHiRes/gfe/*
%dir /awips2/apps/HeatRiskIndex/install/PrismHiRes/install
/awips2/apps/HeatRiskIndex/install/PrismHiRes/install/*
%attr(755,awips,fxalpha) /awips2/apps/HeatRiskIndex/install/HeatRisk/install/install.sh
%attr(755,awips,fxalpha) /awips2/apps/HeatRiskIndex/install/PrismHiRes/install/install.sh
%dir /awips2/apps/HeatRiskIndex/runtime
%defattr(644,root,root,-)

%changelog
* Wed Aug 2 2023 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Added HeatRisk bin directories.

* Fri Sep 9 2022 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Initial package creation.
