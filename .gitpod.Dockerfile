FROM gitpod/workspace-full
RUN sudo apt-get update \
    && sudo apt-get install -y python3 python3-pip python3-crypto libxrender1 libxext6 libxtst6 libfreetype6 libxi6 \
    && sudo rm -rf /var/lib/apt/lists/* \
    && pip3 install projector-installer \
    && mkdir -p ~/.projector/configs # prevent license prompt \
    && projector ide autoinstall --config-name 'idea-ue' --ide-name 'IntelliJ IDEA Ultimate 2021.1.2'1212
