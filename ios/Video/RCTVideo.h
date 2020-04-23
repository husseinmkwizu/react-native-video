#import <AVFoundation/AVFoundation.h>
#import "AVKit/AVKit.h"
#import "UIView+FindUIViewController.h"
#import "RCTVideoPlayerViewController.h"
#import "RCTVideoPlayerViewControllerDelegate.h"
#import <React/RCTComponent.h>
#import <React/RCTBridgeModule.h>

#if __has_include(<react-native-video/RCTVideoCache.h>)
#import <react-native-video/RCTVideoCache.h>
#import <DVAssetLoaderDelegate/DVURLAsset.h>
#import <DVAssetLoaderDelegate/DVAssetLoaderDelegate.h>
#endif

@class RCTEventDispatcher;
#if __has_include(<react-native-video/RCTVideoCache.h>)
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, DVAssetLoaderDelegatesDelegate, AVAssetResourceLoaderDelegate>
#elif TARGET_OS_TV
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, AVAssetResourceLoaderDelegate>
#else
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, AVPictureInPictureControllerDelegate, AVAssetResourceLoaderDelegate>
#endif

@property (nonatomic, copy) RCTDirectEventBlock onVideoLoadStart;
@property (nonatomic, copy) RCTDirectEventBlock onVideoLoad;
@property (nonatomic, copy) RCTDirectEventBlock onVideoBuffer;
@property (nonatomic, copy) RCTDirectEventBlock onVideoError;
@property (nonatomic, copy) RCTDirectEventBlock onVideoProgress;
@property (nonatomic, copy) RCTDirectEventBlock onBandwidthUpdate;
@property (nonatomic, copy) RCTDirectEventBlock onVideoSeek;
@property (nonatomic, copy) RCTDirectEventBlock onVideoEnd;
@property (nonatomic, copy) RCTDirectEventBlock onTimedMetadata;
@property (nonatomic, copy) RCTDirectEventBlock onVideoAudioBecomingNoisy;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerWillPresent;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerDidPresent;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerWillDismiss;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerDidDismiss;
@property (nonatomic, copy) RCTDirectEventBlock onReadyForDisplay;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackStalled;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackResume;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackRateChange;
@property (nonatomic, copy) RCTDirectEventBlock onVideoExternalPlaybackChange;
@property (nonatomic, copy) RCTDirectEventBlock onPictureInPictureStatusChanged;
@property (nonatomic, copy) RCTDirectEventBlock onRestoreUserInterfaceForPictureInPictureStop;
@property (nonatomic, copy) RCTDirectEventBlock onGetLicense;
@property (nonatomic, copy) RCTDirectEventBlock onDRMKeysAcquired;

typedef NS_ENUM(NSInteger, RCTVideoError) {
    RCTVideoErrorFromJSPart = 2001,
    RCTVideoErrorLicenseRequestNotOk = 2002,
    RCTVideoErrorNoDataFromLicenseRequest = 2003,
    RCTVideoErrorNoSPC = 2004,
    RCTVideoErrorNoDataRequest = 2005,
    RCTVideoErrorNoCertificateData = 2006,
    RCTVideoErrorNoCertificateURL = 2007,
    RCTVideoErrorNoFairplayDRM = 2008,
    RCTVideoErrorNoDRMData = 2009,
    
    //---- extra errors
    RCTVideo_ERROR_UNKNOWN = 1001,
    RCTVideo_ERROR_DRM_AUTH_TOKEN_FAILED = 1008,
    RCTVideo_ERROR_DRM_LICENSE_KEY_FAILED = 1009,
};

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

- (AVPlayerViewController*)createPlayerViewController:(AVPlayer*)player withPlayerItem:(AVPlayerItem*)playerItem;

- (void)save:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (void)setLicenseResult:(NSString * )license;
- (BOOL)setLicenseResultError:(NSString * )error;

@end
