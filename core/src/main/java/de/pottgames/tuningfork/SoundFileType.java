package de.pottgames.tuningfork;

public enum SoundFileType {
    OGG, WAV, FLAC;


    public static SoundFileType getByFileEnding(String fileExtension) {
        if ("ogg".equalsIgnoreCase(fileExtension) || "oga".equalsIgnoreCase(fileExtension) || "ogx".equalsIgnoreCase(fileExtension)
                || "opus".equalsIgnoreCase(fileExtension)) {
            return OGG;
        }
        if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            return WAV;
        }
        if ("flac".equalsIgnoreCase(fileExtension)) {
            return FLAC;
        }

        return null;
    }

}
