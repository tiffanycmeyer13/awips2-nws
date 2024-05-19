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
Requires: awips2-heatriskindex-data

%description
AWIPS II Heat Risk Index - Installs AWIPS II HeatRiskIndex Application.

%package data
Summary: Installation of static data used in awips2-heatriskindex
Group: AWIPSII
BuildArch: noarch

%description data
AWIPS II Heat Risk Index - Installs static data used in AWIPS II HeatRiskIndex Application.

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

SRC_DIR="%{_baseline_workspace}/apps/HeatRiskIndex"
rsync --archive ${SRC_DIR}/ ${APP_DIR}/

COMMON_DIR="%{_build_root}/awips2/edex/data/share/HeatRiskIndex"
mkdir --parents ${COMMON_DIR}
mkdir --parents ${COMMON_DIR}/scripts
cp ${APP_DIR}/bin/LoadHeatRisk.sh ${COMMON_DIR}/scripts/.
rm --recursive --force ${APP_DIR}/bin

mkdir -p ${COMMON_DIR}/data
if [ $? -ne 0 ]; then
   exit 1
fi

DATA_SRC_DIR="%{_static_files}/heatrisk"

cp -r ${DATA_SRC_DIR}/heatrisk.hdf5 ${COMMON_DIR}/data/.
if [ $? -ne 0 ]; then
   exit 1
fi

%clean
rm --recursive --force %{_build_root}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/apps/HeatRiskIndex
%dir /awips2/apps/HeatRiskIndex/config
/awips2/apps/HeatRiskIndex/config/*
%dir /awips2/apps/HeatRiskIndex/etc
/awips2/apps/HeatRiskIndex/etc/*
%dir /awips2/edex/data/share/HeatRiskIndex/data

%attr(775,awips,fxalpha) %dir /awips2/edex/data/share/HeatRiskIndex
%attr(775,awips,fxalpha) %dir /awips2/edex/data/share/HeatRiskIndex/scripts
%attr(775,awips,fxalpha) /awips2/edex/data/share/HeatRiskIndex/scripts/LoadHeatRisk.sh
%defattr(644,root,root,-)

%files data
%defattr(775,awips,fxalpha,775)
/awips2/edex/data/share/HeatRiskIndex/data/*

%changelog
* Wed Feb 28 2024 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Removed old structure, added data subpackage

* Tue Oct 17 2023 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Added share directories

* Wed Aug  2 2023 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Added HeatRisk bin directories.

* Fri Sep  9 2022 Michael Gamazaychikov <michael.gamazaychikov@noaa.gov>
- Initial package creation.
